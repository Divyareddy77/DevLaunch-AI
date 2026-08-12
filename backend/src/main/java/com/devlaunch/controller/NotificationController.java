package com.devlaunch.controller;

import com.devlaunch.dto.response.NotificationResponse;
import com.devlaunch.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for the notification center.
 * <p>
 * Exposes endpoints for listing the authenticated user's notifications,
 * retrieving the unread count, marking notifications as read (individually
 * or all at once), and deleting a notification. All endpoints require a
 * valid JWT access token (covered by the default
 * {@code anyRequest().authenticated()} rule in {@code SecurityConfig})
 * and operate exclusively on the authenticated user's own notifications.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Retrieves all notifications belonging to the currently authenticated
     * user, newest first.
     *
     * @return a {@link ResponseEntity} containing the user's notifications
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        return ResponseEntity.ok(notificationService.getNotifications());
    }

    /**
     * Retrieves the number of unread notifications belonging to the
     * currently authenticated user.
     *
     * @return a {@link ResponseEntity} containing the unread count
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    /**
     * Marks a single notification as read, verifying it belongs to the
     * currently authenticated user.
     *
     * @param id the notification ID to mark as read
     * @return a ResponseEntity containing the updated notification
     *         with HTTP status 200 (OK)
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable final Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    /**
     * Marks every notification belonging to the currently authenticated
     * user as read.
     *
     * @return a {@link ResponseEntity} containing the updated notification
     *         list with HTTP status 200 (OK)
     */
    @PutMapping("/read-all")
    public ResponseEntity<List<NotificationResponse>> markAllAsRead() {
        return ResponseEntity.ok(notificationService.markAllAsRead());
    }

    /**
     * Deletes a single notification, verifying it belongs to the currently
     * authenticated user.
     *
     * @param id the notification ID to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable final Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Notification deleted successfully.");
    }

}
