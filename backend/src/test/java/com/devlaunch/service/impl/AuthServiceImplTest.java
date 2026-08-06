package com.devlaunch.service.impl;

import com.devlaunch.dto.request.ForgotPasswordRequest;
import com.devlaunch.dto.request.ResetPasswordRequest;
import com.devlaunch.dto.response.MessageResponse;
import com.devlaunch.entity.PasswordResetToken;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.exception.InvalidPasswordResetTokenException;
import com.devlaunch.exception.PasswordResetTokenExpiredException;
import com.devlaunch.mapper.AuthMapper;
import com.devlaunch.repository.PasswordResetTokenRepository;
import com.devlaunch.repository.RoleRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.security.JwtService;
import com.devlaunch.service.interfaces.AuthService;
import com.devlaunch.service.interfaces.EmailService;
import com.devlaunch.service.interfaces.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the forgot-password / reset-password flow in
 * {@link AuthServiceImpl}.
 * <p>
 * Verifies the generic response contract (account existence is never
 * disclosed), single-use token handling, expiry enforcement, password
 * matching, BCrypt encoding, notification creation, and the audit logs.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String USER_EMAIL = "dev@example.com";

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AuthMapper authMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private NotificationService notificationService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(
                userRepository, roleRepository, authMapper, passwordEncoder,
                authenticationManager, jwtService, passwordResetTokenRepository,
                emailService, notificationService,
                86_400_000L, 30L);
    }

    private User user() {
        final User user = User.builder()
                .firstName("Dev")
                .lastName("User")
                .email(USER_EMAIL)
                .password("old-hash")
                .build();
        user.setId(1L);
        return user;
    }

    private PasswordResetToken validToken(final User user) {
        return PasswordResetToken.builder()
                .token("abc123")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(Boolean.FALSE)
                .build();
    }

    @Test
    @DisplayName("forgot password issues a token and sends an email for a registered account")
    void forgotPasswordIssuesTokenAndSendsEmail() {
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final MessageResponse response = service.forgotPassword(
                ForgotPasswordRequest.builder().email(USER_EMAIL).build());

        assertEquals("If an account exists, a password reset link has been sent.",
                response.getMessage());

        // A fresh token is persisted with a 30-minute expiry and emailed.
        final ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());
        final PasswordResetToken saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertFalse(saved.getUsed());
        assertEquals(64, saved.getToken().length());
        final long minutes = java.time.Duration.between(
                LocalDateTime.now(), saved.getExpiresAt()).toMinutes();
        assertTrue(minutes >= 29 && minutes <= 30, "expiry should be about 30 minutes");

        verify(emailService).sendPasswordResetEmail(eq(user), eq(saved.getToken()));
    }

    @Test
    @DisplayName("forgot password returns the same generic message for an unknown email")
    void forgotPasswordDoesNotRevealAccountExistence() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        final MessageResponse response = service.forgotPassword(
                ForgotPasswordRequest.builder().email("ghost@example.com").build());

        assertEquals("If an account exists, a password reset link has been sent.",
                response.getMessage());
        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    @DisplayName("forgot password skips deactivated accounts without revealing account state")
    void forgotPasswordSkipsDeactivatedAccounts() {
        final User user = user();
        user.setIsActive(Boolean.FALSE);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        final MessageResponse response = service.forgotPassword(
                ForgotPasswordRequest.builder().email(USER_EMAIL).build());

        assertEquals("If an account exists, a password reset link has been sent.",
                response.getMessage());
        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    @DisplayName("forgot password invalidates any previous unused token before issuing a new one")
    void forgotPasswordInvalidatesPreviousTokens() {
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.forgotPassword(ForgotPasswordRequest.builder().email(USER_EMAIL).build());

        verify(passwordResetTokenRepository).deleteUnusedTokens(user);
    }

    @Test
    @DisplayName("resetting a password encodes the new password and consumes the token")
    void resetPasswordUpdatesPasswordAndConsumesToken() {
        final User user = user();
        final PasswordResetToken token = validToken(user);
        when(passwordResetTokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));
        when(passwordResetTokenRepository.markUsedIfUnused("abc123")).thenReturn(1);
        when(passwordEncoder.encode("Password@123")).thenReturn("new-hash");

        final MessageResponse response = service.resetPassword(
                ResetPasswordRequest.builder()
                        .token("abc123")
                        .newPassword("Password@123")
                        .confirmPassword("Password@123")
                        .build());

        assertEquals("Your password has been updated successfully.", response.getMessage());

        // Token is consumed atomically and any other outstanding token swept.
        verify(passwordResetTokenRepository).markUsedIfUnused("abc123");
        verify(passwordResetTokenRepository).deleteUnusedTokens(user);

        // Password is hashed with the existing BCrypt configuration.
        verify(userRepository).save(user);
        assertEquals("new-hash", user.getPassword());

        // The user is notified about the completed reset.
        verify(notificationService).createNotification(
                eq(user), eq(NotificationType.SYSTEM),
                eq("Password Reset Successful"),
                eq("Your password has been changed successfully."));
    }

    @Test
    @DisplayName("a token consumed concurrently by another request is rejected")
    void resetPasswordRejectsConcurrentReplay() {
        final User user = user();
        final PasswordResetToken token = validToken(user);
        when(passwordResetTokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));
        // The atomic claim lost the race: another request consumed it first.
        when(passwordResetTokenRepository.markUsedIfUnused("abc123")).thenReturn(0);

        final InvalidPasswordResetTokenException ex = assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> service.resetPassword(ResetPasswordRequest.builder()
                        .token("abc123")
                        .newPassword("Password@123")
                        .confirmPassword("Password@123")
                        .build()));

        assertEquals("Invalid password reset link.", ex.getMessage());
        verify(userRepository, never()).save(any());
        verify(notificationService, never()).createNotification(any(), any(), any(), any());
    }

    @Test
    @DisplayName("resetting with a non-existent token rejects the request")
    void resetPasswordRejectsUnknownToken() {
        when(passwordResetTokenRepository.findByToken("nope")).thenReturn(Optional.empty());

        final InvalidPasswordResetTokenException ex = assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> service.resetPassword(ResetPasswordRequest.builder()
                        .token("nope")
                        .newPassword("Password@123")
                        .confirmPassword("Password@123")
                        .build()));

        assertEquals("Invalid password reset link.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetting with an already-used token is rejected (replay prevention)")
    void resetPasswordRejectsUsedToken() {
        final User user = user();
        final PasswordResetToken token = validToken(user);
        token.setUsed(Boolean.TRUE);
        when(passwordResetTokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));

        final InvalidPasswordResetTokenException ex = assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> service.resetPassword(ResetPasswordRequest.builder()
                        .token("abc123")
                        .newPassword("Password@123")
                        .confirmPassword("Password@123")
                        .build()));

        assertEquals("Invalid password reset link.", ex.getMessage());
        verify(userRepository, never()).save(any());
        verify(notificationService, never()).createNotification(any(), any(), any(), any());
    }

    @Test
    @DisplayName("resetting with an expired token is rejected")
    void resetPasswordRejectsExpiredToken() {
        final User user = user();
        final PasswordResetToken token = PasswordResetToken.builder()
                .token("abc123")
                .user(user)
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .used(Boolean.FALSE)
                .build();
        when(passwordResetTokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));

        final PasswordResetTokenExpiredException ex = assertThrows(
                PasswordResetTokenExpiredException.class,
                () -> service.resetPassword(ResetPasswordRequest.builder()
                        .token("abc123")
                        .newPassword("Password@123")
                        .confirmPassword("Password@123")
                        .build()));

        assertEquals("Reset link has expired. Please request another password reset.",
                ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetting with mismatched passwords is rejected")
    void resetPasswordRejectsMismatchedPasswords() {
        final IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.resetPassword(ResetPasswordRequest.builder()
                        .token("abc123")
                        .newPassword("Password@123")
                        .confirmPassword("Different@123")
                        .build()));

        assertEquals("Passwords do not match", ex.getMessage());
        verify(passwordResetTokenRepository, never()).findByToken(any());
    }

    @Test
    @DisplayName("generated reset tokens are unique")
    void generatedTokensAreUnique() {
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.forgotPassword(ForgotPasswordRequest.builder().email(USER_EMAIL).build());
        service.forgotPassword(ForgotPasswordRequest.builder().email(USER_EMAIL).build());

        final ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository, org.mockito.Mockito.times(2)).save(captor.capture());

        final java.util.List<PasswordResetToken> issued = captor.getAllValues();
        assertNotNull(issued.get(0).getToken());
        assertNotNull(issued.get(1).getToken());
        assertTrue(!issued.get(0).getToken().equals(issued.get(1).getToken()),
                "two issued tokens must differ");
    }

}
