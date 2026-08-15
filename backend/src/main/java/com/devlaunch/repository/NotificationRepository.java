package com.devlaunch.repository;

import com.devlaunch.entity.Notification;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Notification} entity operations.
 * <p>
 * Provides standard CRUD operations and the user-scoped query methods
 * required by the notification center. Every read is filtered by the
 * owning user so a user can never observe another user's notifications.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Finds all notifications belonging to the specified user, ordered
     * from most recently created to oldest.
     *
     * @param user the user whose notifications to retrieve
     * @return a list of the user's notifications, or an empty list if none exist
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Counts the unread notifications belonging to the specified user.
     *
     * @param user the user whose unread notifications to count
     * @return the number of unread notifications for the user
     */
    long countByUserAndIsReadFalse(User user);

    /**
     * Finds a notification by ID but only if it belongs to the specified
     * user, keeping ownership checks at the query level.
     *
     * @param id   the notification ID to retrieve
     * @param user the user who must own the notification
     * @return an {@link Optional} containing the notification if found and
     *         owned by the user, or empty otherwise
     */
    Optional<Notification> findByIdAndUser(Long id, User user);

    /**
     * Finds all notifications belonging to the specified user.
     * <p>
     * Used when deleting a user so their notifications are removed before
     * the user row, keeping the {@code notifications.user_id} foreign key
     * constraint satisfied.
     * </p>
     *
     * @param user the user whose notifications to retrieve
     * @return a list of the user's notifications, or an empty list if none exist
     */
    List<Notification> findByUser(User user);

    /**
     * Checks whether a notification matching the given title and message
     * was created for the user at or after the given moment.
     * <p>
     * Used by the interview-reminder scheduler to avoid sending the same
     * "interview tomorrow" notification twice for the same interview.
     * </p>
     *
     * @param user       the user to check
     * @param title      the notification title
     * @param message    the exact notification message
     * @param from       the earliest creation moment to consider
     * @return {@code true} if such a notification already exists
     */
    boolean existsByUserAndTitleAndMessageAndCreatedAtGreaterThanEqual(
            User user, String title, String message, LocalDateTime from);

}
