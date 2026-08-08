/**
 * NotificationsWidget — the dashboard's latest-notifications panel.
 *
 * Shows the live unread count plus the newest notifications with their
 * category icon, message, relative timestamp, and an unread indicator.
 * Links to the full notification center.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, ArrowRight } from 'lucide-react';
import { notificationService } from '../../../services/notification.service';
import { NotificationTypeIcon } from '../../notifications/NotificationTypeIcon';
import { AnalyticsCard } from '../analytics/AnalyticsCard';
import { EmptyState } from '../../shared/EmptyState';
import { formatRelativeTime } from '../../../utils/date';
import { truncate } from '../../../utils/format';
import { ROUTES } from '../../../constants/routes';
import type { AppNotification } from '../../../types/notification';

interface NotificationsWidgetProps {
  /** The live unread count from the notification context. */
  unreadCount: number;
}

/** How many of the newest notifications to display. */
const WIDGET_LIMIT = 4;

export const NotificationsWidget: React.FC<NotificationsWidgetProps> = ({ unreadCount }) => {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState<AppNotification[] | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    notificationService
      .getAll()
      .then((all) => setNotifications(all.slice(0, WIDGET_LIMIT)))
      .catch(() => setNotifications(null))
      .finally(() => setLoading(false));
  }, []);

  return (
    <AnalyticsCard
      title="Notifications"
      subtitle={unreadCount > 0 ? `${unreadCount} unread` : 'All caught up'}
      icon={<Bell className="h-5 w-5" />}
      tone={unreadCount > 0 ? 'danger' : 'gray'}
      loading={loading}
      action={
        <button
          type="button"
          onClick={() => navigate(ROUTES.NOTIFICATIONS)}
          className="inline-flex items-center gap-1 text-xs font-medium text-indigo-600 transition-colors hover:text-indigo-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-1"
        >
          View All
          <ArrowRight className="h-3 w-3" />
        </button>
      }
    >
      {!loading && (notifications ?? []).length === 0 ? (
        <EmptyState
          icon={Bell}
          tone="gray"
          title="No notifications"
          description="Updates about your resumes, interviews, and achievements will appear here."
        />
      ) : (
        <ul className="-mx-2 space-y-1">
          {(notifications ?? []).map((notification) => (
            <li
              key={notification.id}
              className={`flex items-start gap-2.5 rounded-xl px-2 py-2 transition-colors hover:bg-gray-50 ${
                notification.isRead ? '' : 'bg-indigo-50/50'
              }`}
            >
              <NotificationTypeIcon type={notification.type} size="sm" />
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-1.5">
                  <p
                    className={`truncate text-xs ${
                      notification.isRead ? 'font-medium text-gray-600' : 'font-semibold text-gray-900'
                    }`}
                  >
                    {notification.title}
                  </p>
                  {!notification.isRead && (
                    <span
                      className="h-1.5 w-1.5 shrink-0 rounded-full bg-indigo-500"
                      role="img"
                      aria-label="Unread"
                    />
                  )}
                </div>
                <p className="mt-0.5 line-clamp-1 text-[11px] text-gray-500">
                  {truncate(notification.message, 90)}
                </p>
                <p className="mt-0.5 text-[10px] text-gray-400">
                  {formatRelativeTime(notification.createdAt)}
                </p>
              </div>
            </li>
          ))}
        </ul>
      )}
    </AnalyticsCard>
  );
};
