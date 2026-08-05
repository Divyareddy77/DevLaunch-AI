/**
 * NotificationsPage — the full notification center.
 *
 * Lists every notification for the authenticated user with an
 * All/Unread filter, click-to-read, mark-all-as-read, and delete.
 * Handles loading, error, and empty states.
 *
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Bell, CheckCheck, RefreshCw, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { notificationService } from '../../services/notification.service';
import { useNotifications } from '../../context/NotificationContext';
import { NotificationTypeIcon } from '../../components/notifications/NotificationTypeIcon';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Card } from '../../components/ui/Card';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { Modal } from '../../components/ui/Modal';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { formatRelativeTime } from '../../utils/date';
import { NOTIFICATION_TYPE_LABELS } from '../../types/notification';
import { MESSAGES } from '../../constants/messages';
import type { AppNotification } from '../../types/notification';

type FilterTab = 'all' | 'unread';

export const NotificationsPage: React.FC = () => {
  const { unreadCount, markAsRead, markAllAsRead, removeNotification } =
    useNotifications();

  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<FilterTab>('all');
  const [isMarkingAll, setIsMarkingAll] = useState(false);

  // Delete confirmation state
  const [deleteTarget, setDeleteTarget] = useState<AppNotification | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchNotifications = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await notificationService.getAll();
      setNotifications(data);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.NOTIFICATIONS_LOAD_ERROR;
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  const visible = useMemo(
    () => (filter === 'unread' ? notifications.filter((n) => !n.isRead) : notifications),
    [notifications, filter],
  );

  const handleOpenNotification = async (notification: AppNotification) => {
    if (notification.isRead) return;
    try {
      await markAsRead(notification.id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === notification.id ? { ...n, isRead: true } : n)),
      );
    } catch {
      toast.error(MESSAGES.SAVE_ERROR('notification'));
    }
  };

  const handleMarkAllRead = async () => {
    setIsMarkingAll(true);
    try {
      await markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      toast.success(MESSAGES.NOTIFICATIONS_ALL_READ);
    } catch {
      toast.error(MESSAGES.SAVE_ERROR('notifications'));
    } finally {
      setIsMarkingAll(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setIsDeleting(true);
    try {
      await removeNotification(deleteTarget.id, !deleteTarget.isRead);
      setNotifications((prev) => prev.filter((n) => n.id !== deleteTarget.id));
      setDeleteTarget(null);
      toast.success(MESSAGES.NOTIFICATION_DELETE_SUCCESS);
    } catch {
      toast.error(MESSAGES.SAVE_ERROR('notification'));
    } finally {
      setIsDeleting(false);
    }
  };

  // ─── Loading state ───
  if (isLoading) {
    return <LoadingScreen />;
  }

  // ─── Error state ───
  if (error) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <ErrorMessage message={error} onRetry={fetchNotifications} />
      </div>
    );
  }

  // ─── Empty state ───
  if (notifications.length === 0) {
    return (
      <div className="mx-auto max-w-3xl">
        <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-gray-200 bg-white px-6 py-16 text-center">
          <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-indigo-100">
            <Bell className="h-8 w-8 text-indigo-600" />
          </div>
          <h2 className="mb-2 text-xl font-semibold text-gray-900">No Notifications</h2>
          <p className="mb-6 max-w-sm text-sm text-gray-500">{MESSAGES.NO_NOTIFICATIONS}</p>
          <Button variant="outline" size="sm" onClick={fetchNotifications}>
            <RefreshCw className="h-4 w-4" />
            Refresh
          </Button>
        </div>
      </div>
    );
  }

  // ─── Data state ───
  return (
    <div className="mx-auto max-w-3xl">
      {/* Page header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Notifications</h1>
          <p className="mt-1 text-sm text-gray-500">
            {unreadCount > 0
              ? `You have ${unreadCount} unread ${unreadCount === 1 ? 'notification' : 'notifications'}.`
              : 'You are all caught up.'}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={fetchNotifications}>
            <RefreshCw className="h-4 w-4" />
            Refresh
          </Button>
          <Button
            size="sm"
            onClick={() => void handleMarkAllRead()}
            loading={isMarkingAll}
            disabled={unreadCount === 0}
          >
            <CheckCheck className="h-4 w-4" />
            Mark all read
          </Button>
        </div>
      </div>

      {/* Filter tabs */}
      <div className="mb-4 flex items-center gap-2">
        <button
          onClick={() => setFilter('all')}
          className={`rounded-full px-3 py-1.5 text-xs font-medium transition-colors ${
            filter === 'all'
              ? 'bg-indigo-100 text-indigo-700 ring-1 ring-indigo-300'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          }`}
        >
          All ({notifications.length})
        </button>
        <button
          onClick={() => setFilter('unread')}
          className={`rounded-full px-3 py-1.5 text-xs font-medium transition-colors ${
            filter === 'unread'
              ? 'bg-indigo-100 text-indigo-700 ring-1 ring-indigo-300'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          }`}
        >
          Unread ({notifications.filter((n) => !n.isRead).length})
        </button>
      </div>

      {/* Filtered-empty state */}
      {visible.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-xl border border-gray-200 bg-white py-12 text-center">
          <Bell className="mb-3 h-10 w-10 text-gray-300" />
          <p className="text-sm font-medium text-gray-500">No unread notifications.</p>
        </div>
      ) : (
        <Card padded={false}>
          <ul className="divide-y divide-gray-100">
            {visible.map((notification) => (
              <li
                key={notification.id}
                className={`flex items-start gap-3 px-4 py-4 transition-colors sm:px-5 ${
                  notification.isRead ? '' : 'bg-indigo-50/40'
                }`}
              >
                <NotificationTypeIcon type={notification.type} />

                <button
                  onClick={() => void handleOpenNotification(notification)}
                  className="min-w-0 flex-1 text-left"
                  title={notification.isRead ? undefined : 'Mark as read'}
                >
                  <div className="flex flex-wrap items-center gap-2">
                    <p
                      className={`text-sm ${
                        notification.isRead
                          ? 'font-medium text-gray-700'
                          : 'font-semibold text-gray-900'
                      }`}
                    >
                      {notification.title}
                    </p>
                    <Badge variant="default" size="sm">
                      {NOTIFICATION_TYPE_LABELS[notification.type]}
                    </Badge>
                    {!notification.isRead && (
                      <span className="h-1.5 w-1.5 rounded-full bg-indigo-500" />
                    )}
                  </div>
                  <p className="mt-1 whitespace-pre-wrap text-sm text-gray-500">
                    {notification.message}
                  </p>
                  <p className="mt-1.5 text-xs text-gray-400">
                    {formatRelativeTime(notification.createdAt)}
                  </p>
                </button>

                <button
                  onClick={() => setDeleteTarget(notification)}
                  title="Delete notification"
                  aria-label={`Delete notification: ${notification.title}`}
                  className="flex-shrink-0 rounded-lg p-1.5 text-gray-300 transition-colors hover:bg-red-50 hover:text-red-500"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </li>
            ))}
          </ul>
        </Card>
      )}

      {/* Delete confirmation modal */}
      <Modal
        isOpen={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        title="Delete Notification"
        closeOnBackdrop={false}
      >
        <p className="text-sm text-gray-600">{MESSAGES.DELETE_CONFIRM('notification')}</p>
        <div className="mt-6 flex justify-end gap-2">
          <Button
            variant="ghost"
            onClick={() => setDeleteTarget(null)}
            disabled={isDeleting}
          >
            Cancel
          </Button>
          <Button variant="danger" onClick={() => void handleDelete()} loading={isDeleting}>
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
};
