package com.devlaunch.messaging.consumer;

import com.devlaunch.entity.User;
import com.devlaunch.messaging.event.ForgotPasswordEmailEvent;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ForgotPasswordEmailConsumer}.
 * <p>
 * Verifies that the consumer delegates to the reusable {@link EmailService}
 * for an existing user and drops the event for a deleted user.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class ForgotPasswordEmailConsumerTest {

    @Mock
    private EmailService emailService;
    @Mock
    private UserRepository userRepository;

    private ForgotPasswordEmailConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ForgotPasswordEmailConsumer(emailService, userRepository);
    }

    @Test
    @DisplayName("delivers the password reset email for an existing user")
    void deliversEmailForExistingUser() {
        final User user = User.builder().email("dev@example.com").build();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        consumer.onForgotPasswordEmail(new ForgotPasswordEmailEvent(1L, "token-123"));

        verify(emailService).sendPasswordResetEmail(user, "token-123");
    }

    @Test
    @DisplayName("drops the email for a user that no longer exists")
    void dropsEmailForUnknownUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        consumer.onForgotPasswordEmail(new ForgotPasswordEmailEvent(99L, "token-123"));

        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

}
