package com.devlaunch.messaging.consumer;

import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.NotificationService;
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
 * Unit tests for {@link NotificationEventProcessor}.
 * <p>
 * Verifies that an event for an existing user is persisted through the
 * reusable {@link NotificationService} and that an event for a deleted
 * user is dropped without ever being retried.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class NotificationEventProcessorTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private UserRepository userRepository;

    private NotificationEventProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new NotificationEventProcessor(notificationService, userRepository);
    }

    @Test
    @DisplayName("persists the notification for an existing user via the notification module")
    void persistsNotificationForExistingUser() {
        final User user = User.builder().email("dev@example.com").build();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        processor.process(new NotificationEvent(1L, NotificationType.SYSTEM, "Title", "Message"));

        verify(notificationService)
                .createNotification(user, NotificationType.SYSTEM, "Title", "Message");
    }

    @Test
    @DisplayName("drops the notification for a user that no longer exists")
    void dropsNotificationForUnknownUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        processor.process(new NotificationEvent(99L, NotificationType.SYSTEM, "Title", "Message"));

        verify(notificationService, never()).createNotification(any(), any(), any(), any());
    }

}
