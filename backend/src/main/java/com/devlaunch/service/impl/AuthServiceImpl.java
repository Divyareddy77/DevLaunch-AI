package com.devlaunch.service.impl;

import com.devlaunch.dto.request.ForgotPasswordRequest;
import com.devlaunch.dto.request.LoginRequest;
import com.devlaunch.dto.request.RegisterRequest;
import com.devlaunch.dto.request.ResetPasswordRequest;
import com.devlaunch.dto.response.AuthResponse;
import com.devlaunch.dto.response.MessageResponse;
import com.devlaunch.dto.response.UserResponse;
import com.devlaunch.entity.PasswordResetToken;
import com.devlaunch.entity.Role;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.entity.enums.RoleType;
import com.devlaunch.exception.EmailAlreadyExistsException;
import com.devlaunch.exception.InvalidCredentialsException;
import com.devlaunch.exception.InvalidPasswordResetTokenException;
import com.devlaunch.exception.PasswordResetTokenExpiredException;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.AuthMapper;
import com.devlaunch.messaging.EventPublisher;
import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.ForgotPasswordEmailEvent;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.repository.PasswordResetTokenRepository;
import com.devlaunch.repository.RoleRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.security.CustomUserDetailsService;
import com.devlaunch.security.JwtService;
import com.devlaunch.service.interfaces.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Implementation of {@link AuthService} providing user registration
 * and login functionality with JWT-based authentication.
 * <p>
 * Handles email uniqueness validation, password encoding, role
 * assignment for new users, and credential verification during login.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    /**
     * Message returned for every forgot-password request, whether or not
     * the email is registered, so account existence is never disclosed.
     */
    private static final String FORGOT_PASSWORD_MESSAGE =
            "If an account exists, a password reset link has been sent.";

    /**
     * Message returned when a password reset completes successfully.
     */
    private static final String RESET_PASSWORD_MESSAGE =
            "Your password has been updated successfully.";

    /**
     * The message shown for tokens that do not exist or were already used.
     */
    private static final String INVALID_TOKEN_MESSAGE = "Invalid password reset link.";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EventPublisher eventPublisher;
    private final long jwtExpiration;
    private final long resetTokenExpiryMinutes;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Constructs the authentication service with all required dependencies.
     *
     * @param userRepository           repository for user data access
     * @param roleRepository           repository for role data access
     * @param authMapper               mapper for DTO-entity conversions
     * @param passwordEncoder               encoder for hashing user passwords
     * @param authenticationManager         Spring Security authentication manager
     * @param jwtService                    service for JWT token operations
     * @param passwordResetTokenRepository  repository for one-time reset tokens
     * @param eventPublisher                publisher for the messaging backbone
     * @param jwtExpiration                 JWT token expiration duration in milliseconds
     * @param resetTokenExpiryMinutes       how long reset tokens remain valid, in minutes
     */
    public AuthServiceImpl(final UserRepository userRepository,
                           final RoleRepository roleRepository,
                           final AuthMapper authMapper,
                           final PasswordEncoder passwordEncoder,
                           final AuthenticationManager authenticationManager,
                           final JwtService jwtService,
                           final PasswordResetTokenRepository passwordResetTokenRepository,
                           final EventPublisher eventPublisher,
                           @Value("${jwt.expiration}") final long jwtExpiration,
                           @Value("${devlaunch.auth.reset-token-expiry-minutes}") final long resetTokenExpiryMinutes) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.eventPublisher = eventPublisher;
        this.jwtExpiration = jwtExpiration;
        this.resetTokenExpiryMinutes = resetTokenExpiryMinutes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserResponse register(final RegisterRequest request) {
        // Check if the email is already in use
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "User with email " + request.getEmail() + " already exists");
        }

        // Load the default STUDENT role
        final Role studentRole = roleRepository.findByRoleName(RoleType.STUDENT)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role STUDENT not found in the system"));

        // Map request DTO to a new User entity
        final User user = authMapper.toUser(request);

        // Encode and set the password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign the STUDENT role and activate the account
        user.setRole(studentRole);
        user.setIsActive(true);

        // Persist the new user
        final User savedUser = userRepository.save(user);

        // Return the user profile without sensitive data
        return authMapper.toUserResponse(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(final LoginRequest request) {
        // Authenticate the user credentials
        final Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (final AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Extract the authenticated user's details from the authentication result
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Generate a JWT access token
        final String accessToken = jwtService.generateToken(userDetails);

        // Build and return the authentication response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .message("Login successful")
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MessageResponse forgotPassword(final ForgotPasswordRequest request) {
        final String email = request.getEmail().trim();
        final Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent() && Boolean.TRUE.equals(user.get().getIsActive())) {
            issueResetToken(user.get());
            log.info("Password reset requested for user id={}", user.get().getId());
        } else if (user.isPresent()) {
            // A deactivated account must not receive a usable reset link,
            // but the response stays identical to hide account state.
            log.info("Password reset requested for an inactive account");
        } else {
            // Audit the attempt without ever revealing account existence to
            // the caller; the response is identical either way.
            log.info("Password reset requested for an unregistered email");
        }

        return new MessageResponse(FORGOT_PASSWORD_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MessageResponse resetPassword(final ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        final PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new InvalidPasswordResetTokenException(INVALID_TOKEN_MESSAGE));

        // A consumed token is permanently invalid — replaying the link must
        // fail even if the token has not yet expired.
        if (Boolean.TRUE.equals(resetToken.getUsed())) {
            throw new InvalidPasswordResetTokenException(INVALID_TOKEN_MESSAGE);
        }
        if (resetToken.isExpired()) {
            throw new PasswordResetTokenExpiredException(
                    "Reset link has expired. Please request another password reset.");
        }

        // Atomically consume the token before changing anything. If a
        // concurrent request already used it, this returns 0 and the reset
        // is rejected — closing the check-then-set replay race.
        final int consumed = passwordResetTokenRepository.markUsedIfUnused(resetToken.getToken());
        if (consumed == 0) {
            throw new InvalidPasswordResetTokenException(INVALID_TOKEN_MESSAGE);
        }

        final User user = resetToken.getUser();

        // Hash the new password with the existing BCrypt configuration.
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Sweep any other outstanding tokens so no other link for the user
        // remains usable. The claimed token is already used=true, so it is
        // not affected by this deletion.
        passwordResetTokenRepository.deleteUnusedTokens(user);

        // The confirmation notification is published as an event and
        // persisted asynchronously by the messaging consumer.
        eventPublisher.publish(EventTopics.PASSWORD_RESET_SUCCESS_KEY,
                new NotificationEvent(user.getId(), NotificationType.SYSTEM,
                        "Password Reset Successful",
                        "Your password has been changed successfully."));

        log.info("Password reset completed for user id={}", user.getId());

        return new MessageResponse(RESET_PASSWORD_MESSAGE);
    }

    /**
     * Generates, persists, and emails a new one-time reset token for the
     * given user, invalidating any previous unused token first.
     *
     * @param user the user requesting the reset
     */
    private void issueResetToken(final User user) {
        // Only the most recent link should stay valid.
        passwordResetTokenRepository.deleteUnusedTokens(user);

        final PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(generateResetToken())
                .expiresAt(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes))
                .used(Boolean.FALSE)
                .build();
        passwordResetTokenRepository.save(resetToken);

        // Tokens are high-entropy and never logged; the email is the only
        // place they appear.
        eventPublisher.publish(EventTopics.FORGOT_PASSWORD_EMAIL_KEY,
                new ForgotPasswordEmailEvent(user.getId(), resetToken.getToken()));
    }

    /**
     * Generates a cryptographically secure random token.
     * <p>
     * 32 random bytes rendered as 64 hex characters — sufficient entropy
     * that tokens cannot be guessed or enumerated.
     * </p>
     *
     * @return the hex-encoded token
     */
    private String generateResetToken() {
        final byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

}
