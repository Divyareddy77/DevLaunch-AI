package com.devlaunch.service.impl;

import com.devlaunch.dto.request.LoginRequest;
import com.devlaunch.dto.request.RegisterRequest;
import com.devlaunch.dto.response.AuthResponse;
import com.devlaunch.dto.response.UserResponse;
import com.devlaunch.entity.Role;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.RoleType;
import com.devlaunch.exception.EmailAlreadyExistsException;
import com.devlaunch.exception.InvalidCredentialsException;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.AuthMapper;
import com.devlaunch.repository.RoleRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.security.CustomUserDetailsService;
import com.devlaunch.security.JwtService;
import com.devlaunch.service.interfaces.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long jwtExpiration;

    /**
     * Constructs the authentication service with all required dependencies.
     *
     * @param userRepository           repository for user data access
     * @param roleRepository           repository for role data access
     * @param authMapper               mapper for DTO-entity conversions
     * @param passwordEncoder          encoder for hashing user passwords
     * @param authenticationManager    Spring Security authentication manager
     * @param jwtService               service for JWT token operations
     * @param jwtExpiration            JWT token expiration duration in milliseconds
     */
    public AuthServiceImpl(final UserRepository userRepository,
                           final RoleRepository roleRepository,
                           final AuthMapper authMapper,
                           final PasswordEncoder passwordEncoder,
                           final AuthenticationManager authenticationManager,
                           final JwtService jwtService,
                           @Value("${jwt.expiration}") final long jwtExpiration) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtExpiration = jwtExpiration;
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

}
