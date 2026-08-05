/**
 * Notification service.
 *
 * Provides the notification center methods backed by the authenticated
 * /api/notifications endpoints. Every endpoint operates exclusively on
 * the currently logged-in user's own notifications.
 *
 * @see backend/src/main/java/com/devlaunch/controller/NotificationController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { NOTIFICATIONS } from '../api/endpoints';
import type { AppNotification } from '../types/notification';

export const notificationService = {
  /**
   * Retrieves all notifications for the authenticated user, newest first.
   *
   * GET /api/notifications
   */
  getAll: () =>
    apiClient.get<AppNotification[]>(NOTIFICATIONS.BASE).then((res) => res.data),

  /**
   * Retrieves the unread notification count for the authenticated user.
   *
   * GET /api/notifications/unread-count
   */
  getUnreadCount: () =>
    apiClient.get<number>(NOTIFICATIONS.UNREAD_COUNT).then((res) => res.data),

  /**
   * Marks a single notification as read.
   *
   * PUT /api/notifications/{id}/read
   */
  markAsRead: (id: number) =>
    apiClient.put<AppNotification>(NOTIFICATIONS.MARK_READ(id)).then((res) => res.data),

  /**
   * Marks every notification of the authenticated user as read.
   *
   * PUT /api/notifications/read-all
   */
  markAllAsRead: () =>
    apiClient.put<AppNotification[]>(NOTIFICATIONS.READ_ALL).then((res) => res.data),

  /**
   * Deletes a single notification.
   *
   * DELETE /api/notifications/{id}
   */
  deleteNotification: (id: number) =>
    apiClient.delete<void>(NOTIFICATIONS.BY_ID(id)).then(() => undefined),
};
