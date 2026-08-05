package com.devlaunch.service.impl;

import com.devlaunch.dto.response.NotificationResponse;
import com.devlaunch.entity.Notification;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.repository.NotificationRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link NotificationService} providing the notification
 * center for DevLaunch users.
 * <p>
 * Notifications are created automatically by the existing platform modules
 * through {@link #createNotification(User, NotificationType, String, String)}
 * and admin announcements fan out to every user through
 * {@link #notifyAllUsers(NotificationType, String, String)}. Inbox
 * operations use the Spring Security {@link SecurityContextHolder} to
 * resolve the authenticated user and every repository query is filtered by
 * that user, so a user can only ever see, mark, or delete their own
 * notifications.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the notification service with the required repositories.
     *
     * @param notificationRepository repository for notification data access
     * @param userRepository         repository for user data access
     */
    public NotificationServiceImpl(final NotificationRepository notificationRepository,
                                   final UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void createNotification(final User user, final NotificationType type,
                                   final String title, final String message) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .isRead(Boolean.FALSE)
                .build());

        log.debug("Notification created for user id={}, type={}: {}",
                user.getId(), type, title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void notifyAllUsers(final NotificationType type, final String title,
                               final String message) {
        final List<User> users = userRepository.findByIsActiveTrue();
        users.forEach(user -> createNotification(user, type, title, message));

        log.info("Announcement notification sent to {} active users: {}",
                users.size(), title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications() {
        final User user = getAuthenticatedUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        final User user = getAuthenticatedUser();
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public NotificationResponse markAsRead(final Long id) {
        final User user = getAuthenticatedUser();
        final Notification notification = notificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification with id " + id + " not found for the authenticated user"));

        if (!notification.getIsRead()) {
            notification.setIsRead(Boolean.TRUE);
            notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<NotificationResponse> markAllAsRead() {
        final User user = getAuthenticatedUser();
        final List<Notification> notifications =
                notificationRepository.findByUserOrderByCreatedAtDesc(user);

        notifications.stream()
                .filter(notification -> !notification.getIsRead())
                .forEach(notification -> notification.setIsRead(Boolean.TRUE));
        notificationRepository.saveAll(notifications);

        return notifications.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteNotification(final Long id) {
        final User user = getAuthenticatedUser();
        final Notification notification = notificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification with id " + id + " not found for the authenticated user"));

        notificationRepository.delete(notification);

        log.debug("Notification id={} deleted for user id={}", id, user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAllForUser(final User user) {
        notificationRepository.deleteAll(notificationRepository.findByUser(user));
    }

    /**
     * Maps a {@link Notification} entity to its response DTO.
     *
     * @param notification the notification entity to map
     * @return the response DTO for the notification
     */
    private NotificationResponse toResponse(final Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    /**
     * Retrieves the currently authenticated user from the database.
     * <p>
     * Extracts the username (email) from the {@link SecurityContextHolder},
     * fetches the corresponding {@link User} entity from the repository,
     * and throws a {@link ResourceNotFoundException} if no matching user
     * is found.
     * </p>
     *
     * @return the authenticated {@link User} entity
     * @throws ResourceNotFoundException if the user is not found in the database
     */
    private User getAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + email + " not found"));
    }

}
