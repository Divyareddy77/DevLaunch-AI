package com.devlaunch.service.interfaces;

import com.devlaunch.dto.response.NotificationResponse;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Service interface for the notification center.
 * <p>
 * Defines the contract for creating notifications automatically from the
 * existing platform modules, fanning out admin announcements to every
 * user, and managing the authenticated user's own notification inbox
 * (listing, unread counting, marking read, and deleting). All read and
 * write operations are scoped to the authenticated user so a user can
 * never access another user's notifications.
 * </p>
 *
 * @author DevLaunch
 */
public interface NotificationService {

    /**
     * Creates a notification for the given user.
     * <p>
     * Called internally by the existing modules (resume builder, job
     * tracker, study planner, AI features, admin announcements) whenever
     * an important action completes. New notifications always start unread.
     * </p>
     *
     * @param user    the user to notify
     * @param type    the notification category
     * @param title   the notification headline
     * @param message the notification body
     */
    void createNotification(User user, NotificationType type, String title, String message);

    /**
     * Creates an announcement notification for every registered user.
     * <p>
     * Used by the admin module when an announcement is published so all
     * users receive it as a notification without duplicating the
     * announcement storage itself.
     * </p>
     *
     * @param type    the notification category (typically {@code ANNOUNCEMENT})
     * @param title   the notification headline
     * @param message the notification body
     */
    void notifyAllUsers(NotificationType type, String title, String message);

    /**
     * Retrieves all notifications belonging to the currently authenticated
     * user, newest first.
     *
     * @return a list of the authenticated user's notifications,
     *         or an empty list if none exist
     */
    List<NotificationResponse> getNotifications();

    /**
     * Counts the unread notifications of the currently authenticated user.
     *
     * @return the number of unread notifications
     */
    long getUnreadCount();

    /**
     * Marks a single notification as read, verifying it belongs to the
     * currently authenticated user.
     *
     * @param id the notification ID to mark as read
     * @return the updated notification data
     * @throws ResourceNotFoundException if the notification is not found
     *                                   or does not belong to the user
     */
    NotificationResponse markAsRead(Long id);

    /**
     * Marks every notification of the currently authenticated user as read.
     *
     * @return the updated notification list, newest first
     */
    List<NotificationResponse> markAllAsRead();

    /**
     * Deletes a single notification, verifying it belongs to the currently
     * authenticated user.
     *
     * @param id the notification ID to delete
     * @throws ResourceNotFoundException if the notification is not found
     *                                   or does not belong to the user
     */
    void deleteNotification(Long id);

    /**
     * Deletes every notification belonging to the given user.
     * <p>
     * Used by the admin module when deleting a user so the
     * {@code notifications.user_id} foreign key constraint stays valid.
     * </p>
     *
     * @param user the user whose notifications to delete
     */
    void deleteAllForUser(User user);

}
