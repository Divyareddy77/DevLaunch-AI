/**
 * NotificationContext — shared notification state for the application shell.
 *
 * Exposes the unread notification count so the bell badge, the dashboard
 * card, and the notifications page stay in sync, along with actions that
 * update the count immediately after the corresponding API call succeeds.
 *
 * The provider mounts inside the authenticated DashboardLayout, so the
 * unread count is fetched only for logged-in users.
 *
 * @author DevLaunch
 */

import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { notificationService } from '../services/notification.service';

interface NotificationContextValue {
  /** The current number of unread notifications for the user. */
  unreadCount: number;
  /** Refetches the unread count from the backend. */
  refreshUnreadCount: () => Promise<void>;
  /** Marks a single notification as read and decrements the count. */
  markAsRead: (id: number) => Promise<void>;
  /** Marks every notification as read and zeroes the count. */
  markAllAsRead: () => Promise<void>;
  /** Deletes a notification, decrementing the count if it was unread. */
  removeNotification: (id: number, wasUnread: boolean) => Promise<void>;
}

const NotificationContext = createContext<NotificationContextValue | undefined>(undefined);

/** Polling interval for the unread count badge, in milliseconds. */
const UNREAD_POLL_INTERVAL_MS = 60_000;

export const NotificationProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [unreadCount, setUnreadCount] = useState(0);

  const refreshUnreadCount = useCallback(async () => {
    try {
      const count = await notificationService.getUnreadCount();
      setUnreadCount(count);
    } catch {
      // Notifications are supplementary — keep the previous count on failure.
    }
  }, []);

  // Fetch on mount and keep the badge fresh while the app is open.
  useEffect(() => {
    refreshUnreadCount();
    const interval = window.setInterval(refreshUnreadCount, UNREAD_POLL_INTERVAL_MS);
    return () => window.clearInterval(interval);
  }, [refreshUnreadCount]);

  const markAsRead = useCallback(async (id: number) => {
    const updated = await notificationService.markAsRead(id);
    if (!updated.isRead) return;
    setUnreadCount((prev) => Math.max(0, prev - 1));
  }, []);

  const markAllAsRead = useCallback(async () => {
    await notificationService.markAllAsRead();
    setUnreadCount(0);
  }, []);

  const removeNotification = useCallback(async (id: number, wasUnread: boolean) => {
    await notificationService.deleteNotification(id);
    if (wasUnread) {
      setUnreadCount((prev) => Math.max(0, prev - 1));
    }
  }, []);

  const value = useMemo<NotificationContextValue>(
    () => ({
      unreadCount,
      refreshUnreadCount,
      markAsRead,
      markAllAsRead,
      removeNotification,
    }),
    [unreadCount, refreshUnreadCount, markAsRead, markAllAsRead, removeNotification],
  );

  return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>;
};

/** Hook to access the notification context within the application shell. */
export function useNotifications(): NotificationContextValue {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
}
