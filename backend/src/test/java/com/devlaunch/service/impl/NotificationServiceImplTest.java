package com.devlaunch.service.impl;

import com.devlaunch.dto.response.NotificationResponse;
import com.devlaunch.entity.Notification;
import com.devlaunch.entity.Role;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.entity.enums.RoleType;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.repository.NotificationRepository;
import com.devlaunch.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NotificationServiceImpl}.
 * <p>
 * Verifies automatic notification creation, announcement fan-out to all
 * users, and the authenticated user's inbox operations (listing, unread
 * counting, marking read, deleting) with strict ownership scoping.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    private static final String USER_EMAIL = "jane@example.com";

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationRepository, userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(USER_EMAIL, null, List.of()));
    }

    private User user(final long id) {
        final Role role = Role.builder().roleName(RoleType.STUDENT).build();
        final User user = User.builder()
                .firstName("Jane").lastName("Doe")
                .email(USER_EMAIL).isActive(true).role(role)
                .build();
        user.setId(id);
        return user;
    }

    private Notification notification(final long id, final boolean read) {
        final Notification notification = Notification.builder()
                .user(user(2L))
                .type(NotificationType.RESUME)
                .title("Resume created")
                .message("Your resume was created successfully.")
                .isRead(read)
                .build();
        notification.setId(id);
        notification.setCreatedAt(LocalDateTime.of(2026, 8, 5, 10, 0));
        return notification;
    }

    // ─── Automatic creation ─────────────────────────────────────────────────

    @Test
    @DisplayName("creating a notification persists an unread record for the user")
    void createNotificationPersistsUnreadRecord() {
        final User jane = user(2L);

        service.createNotification(jane, NotificationType.RESUME,
                "Resume created", "Your resume was created successfully.");

        final ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(jane, captor.getValue().getUser());
        assertEquals(NotificationType.RESUME, captor.getValue().getType());
        assertEquals("Resume created", captor.getValue().getTitle());
        assertFalse(captor.getValue().getIsRead());
    }

    @Test
    @DisplayName("notifying all users creates one notification per active user")
    void notifyAllUsersCreatesNotificationPerActiveUser() {
        final User jane = user(2L);
        final User john = user(3L);
        when(userRepository.findByIsActiveTrue()).thenReturn(List.of(jane, john));

        service.notifyAllUsers(NotificationType.ANNOUNCEMENT,
                "New feature", "Check out what's new.");

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("deleting all notifications for a user removes their inbox")
    void deleteAllForUserRemovesInbox() {
        final User jane = user(2L);
        final List<Notification> inbox = List.of(notification(1L, false));
        when(notificationRepository.findByUser(jane)).thenReturn(inbox);

        service.deleteAllForUser(jane);

        verify(notificationRepository).deleteAll(inbox);
    }

    // ─── Listing and counting ───────────────────────────────────────────────

    @Test
    @DisplayName("notifications are listed newest first for the authenticated user")
    void getNotificationsListsForAuthenticatedUser() {
        authenticateAsUser();
        final User jane = user(2L);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(jane));
        when(notificationRepository.findByUserOrderByCreatedAtDesc(jane))
                .thenReturn(List.of(notification(1L, false)));

        final List<NotificationResponse> responses = service.getNotifications();

        assertEquals(1, responses.size());
        assertEquals("Resume created", responses.get(0).getTitle());
        assertEquals(NotificationType.RESUME, responses.get(0).getType());
        assertEquals(Boolean.FALSE, responses.get(0).getIsRead());
    }

    @Test
    @DisplayName("unread count reflects the authenticated user's unread notifications")
    void getUnreadCountForAuthenticatedUser() {
        authenticateAsUser();
        final User jane = user(2L);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(jane));
        when(notificationRepository.countByUserAndIsReadFalse(jane)).thenReturn(3L);

        assertEquals(3L, service.getUnreadCount());
    }

    // ─── Marking as read ────────────────────────────────────────────────────

    @Test
    @DisplayName("marking a notification as read updates and returns it")
    void markAsReadUpdatesAndReturns() {
        authenticateAsUser();
        final User jane = user(2L);
        final Notification unread = notification(1L, false);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(jane));
        when(notificationRepository.findByIdAndUser(1L, jane)).thenReturn(Optional.of(unread));
        when(notificationRepository.save(unread)).thenReturn(unread);

        final NotificationResponse response = service.markAsRead(1L);

        assertTrue(unread.getIsRead());
        assertTrue(response.getIsRead());
    }

    @Test
    @DisplayName("marking a notification owned by another user throws")
    void markAsReadForAnotherUsersNotificationThrows() {
        authenticateAsUser();
        final User jane = user(2L);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(jane));
        when(notificationRepository.findByIdAndUser(99L, jane)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.markAsRead(99L));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("marking all notifications as read updates every unread one")
    void markAllAsReadUpdatesEveryUnread() {
        authenticateAsUser();
        final User jane = user(2L);
        final List<Notification> inbox = List.of(
                notification(1L, false),
                notification(2L, true));
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(jane));
        when(notificationRepository.findByUserOrderByCreatedAtDesc(jane))
                .thenReturn(inbox);

        final List<NotificationResponse> responses = service.markAllAsRead();

        assertTrue(inbox.get(0).getIsRead());
        assertTrue(inbox.get(1).getIsRead());
        assertEquals(2, responses.size());
        verify(notificationRepository).saveAll(inbox);
    }

    // ─── Deletion ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleting an owned notification removes it")
    void deleteNotificationRemovesOwned() {
        authenticateAsUser();
        final User jane = user(2L);
        final Notification owned = notification(1L, false);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(jane));
        when(notificationRepository.findByIdAndUser(1L, jane)).thenReturn(Optional.of(owned));

        service.deleteNotification(1L);

        verify(notificationRepository).delete(owned);
    }

    @Test
    @DisplayName("deleting a notification owned by another user throws")
    void deleteNotificationForAnotherUsersNotificationThrows() {
        authenticateAsUser();
        final User jane = user(2L);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(jane));
        when(notificationRepository.findByIdAndUser(99L, jane)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteNotification(99L));
        verify(notificationRepository, never()).delete(any(Notification.class));
    }

}
