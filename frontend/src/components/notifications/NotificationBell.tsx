/**
 * NotificationBell — the header bell with an unread badge and dropdown.
 *
 * Shows the live unread count from the notification context. Clicking the
 * bell opens a dropdown listing the most recent notifications (title,
 * message, relative time, read/unread indicator, and category). Clicking
 * a notification marks it as read; "View all" opens the notification
 * center page. Closes on outside click or Escape.
 *
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, CheckCheck, ArrowRight } from 'lucide-react';
import { notificationService } from '../../services/notification.service';
import { useNotifications } from '../../context/NotificationContext';
import { NotificationTypeIcon } from './NotificationTypeIcon';
import { Badge } from '../ui/Badge';
import { Spinner } from '../ui/Spinner';
import { formatRelativeTime } from '../../utils/date';
import { truncate } from '../../utils/format';
import { MESSAGES } from '../../constants/messages';
import { ROUTES } from '../../constants/routes';
import type { AppNotification } from '../../types/notification';

/** How many of the most recent notifications the dropdown shows. */
const DROPDOWN_LIMIT = 8;

export const NotificationBell: React.FC = () => {
  const navigate = useNavigate();
  const { unreadCount, markAsRead, markAllAsRead } = useNotifications();

  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [loadError, setLoadError] = useState(false);

  const containerRef = useRef<HTMLDivElement>(null);

  // Fetch the latest notifications each time the dropdown opens.
  const loadNotifications = useCallback(async () => {
    setIsLoading(true);
    setLoadError(false);
    try {
      const data = await notificationService.getAll();
      setNotifications(data.slice(0, DROPDOWN_LIMIT));
    } catch {
      setLoadError(true);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const handleToggle = () => {
    const next = !isOpen;
    setIsOpen(next);
    if (next) {
      void loadNotifications();
    }
  };

  // Close on outside click or Escape.
  useEffect(() => {
    if (!isOpen) return;

    const handlePointerDown = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setIsOpen(false);
    };

    document.addEventListener('mousedown', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('mousedown', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen]);

  const handleOpenNotification = async (notification: AppNotification) => {
    if (!notification.isRead) {
      try {
        await markAsRead(notification.id);
      } catch {
        // Keep the dropdown usable even if marking fails.
      }
    }
    setIsOpen(false);
  };

  const handleMarkAllRead = async () => {
    try {
      await markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch {
      // The badge stays unchanged when the request fails.
    }
  };

  return (
    <div ref={containerRef} className="relative">
      {/* Bell button with unread badge */}
      <button
        onClick={handleToggle}
        aria-label="Notifications"
        aria-expanded={isOpen}
        className={`relative flex h-9 w-9 items-center justify-center rounded-full transition-colors ${
          isOpen ? 'bg-gray-100 text-gray-900' : 'text-gray-500 hover:bg-gray-100 hover:text-gray-700'
        }`}
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-semibold text-white shadow-sm">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown panel */}
      {isOpen && (
        <div className="absolute right-0 top-full z-50 mt-2 w-96 max-w-[calc(100vw-2rem)] overflow-hidden rounded-xl border border-gray-200 bg-white shadow-lg">
          {/* Header */}
          <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-semibold text-gray-900">Notifications</h3>
              {unreadCount > 0 && (
                <Badge variant="danger" size="sm">
                  {unreadCount} unread
                </Badge>
              )}
            </div>
            {unreadCount > 0 && (
              <button
                onClick={() => void handleMarkAllRead()}
                className="inline-flex items-center gap-1 text-xs font-medium text-indigo-600 transition-colors hover:text-indigo-800"
              >
                <CheckCheck className="h-3.5 w-3.5" />
                Mark all read
              </button>
            )}
          </div>

          {/* Body */}
          <div className="max-h-96 overflow-y-auto">
            {isLoading ? (
              <div className="flex items-center justify-center py-10">
                <Spinner size="md" label="Loading…" />
              </div>
            ) : loadError ? (
              <div className="px-4 py-10 text-center">
                <p className="text-sm text-gray-500">{MESSAGES.NOTIFICATIONS_LOAD_ERROR}</p>
                <button
                  onClick={() => void loadNotifications()}
                  className="mt-2 text-xs font-medium text-indigo-600 hover:text-indigo-800"
                >
                  Try again
                </button>
              </div>
            ) : notifications.length === 0 ? (
              <div className="px-4 py-10 text-center">
                <p className="text-sm text-gray-500">{MESSAGES.NO_NOTIFICATIONS}</p>
              </div>
            ) : (
              <ul className="divide-y divide-gray-100">
                {notifications.map((notification) => (
                  <li key={notification.id}>
                    <button
                      onClick={() => void handleOpenNotification(notification)}
                      className={`flex w-full items-start gap-3 px-4 py-3 text-left transition-colors hover:bg-gray-50 ${
                        notification.isRead ? '' : 'bg-indigo-50/50'
                      }`}
                    >
                      <NotificationTypeIcon type={notification.type} size="sm" />
                      <div className="min-w-0 flex-1">
                        <div className="flex items-center gap-2">
                          <p
                            className={`truncate text-sm ${
                              notification.isRead
                                ? 'font-medium text-gray-700'
                                : 'font-semibold text-gray-900'
                            }`}
                          >
                            {notification.title}
                          </p>
                          {!notification.isRead && (
                            <span className="h-1.5 w-1.5 flex-shrink-0 rounded-full bg-indigo-500" />
                          )}
                        </div>
                        <p className="mt-0.5 line-clamp-2 text-xs text-gray-500">
                          {truncate(notification.message, 120)}
                        </p>
                        <p className="mt-1 text-[11px] text-gray-400">
                          {formatRelativeTime(notification.createdAt)}
                        </p>
                      </div>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* Footer */}
          <div className="border-t border-gray-100 px-4 py-2.5">
            <button
              onClick={() => {
                setIsOpen(false);
                navigate(ROUTES.NOTIFICATIONS);
              }}
              className="flex w-full items-center justify-center gap-1 rounded-lg py-1.5 text-xs font-medium text-indigo-600 transition-colors hover:bg-indigo-50 hover:text-indigo-800"
            >
              View all notifications
              <ArrowRight className="h-3.5 w-3.5" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
