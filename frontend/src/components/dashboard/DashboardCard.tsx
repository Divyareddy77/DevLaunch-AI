/**
 * DashboardCard — a generic card wrapper for dashboard widget sections.
 *
 * Provides consistent padding, title, icon, and an optional action area
 * so every dashboard widget shares the same base styling.
 *
 * @author DevLaunch
 */

import React, { type ReactNode } from 'react';

interface DashboardCardProps {
  /** The card title displayed at the top. */
  title: string;
  /** Optional icon rendered to the left of the title. */
  icon?: ReactNode;
  /** Optional action element placed on the right side of the header (e.g. a link or button). */
  action?: ReactNode;
  /** Whether the card body is currently loading (shows a subtle skeleton state). */
  loading?: boolean;
  /** The main content rendered inside the card body. */
  children: ReactNode;
  /** Optional additional CSS classes. */
  className?: string;
}

export const DashboardCard: React.FC<DashboardCardProps> = ({
  title,
  icon,
  action,
  loading = false,
  children,
  className = '',
}) => {
  return (
    <div
      className={`rounded-xl border border-gray-200 bg-white shadow-sm transition-shadow hover:shadow-md ${className}`}
    >
      {/* Card header */}
      <div className="flex items-center justify-between border-b border-gray-100 px-5 py-4">
        <div className="flex items-center gap-2">
          {icon && (
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
              {icon}
            </span>
          )}
          <h3 className="text-sm font-semibold text-gray-800">{title}</h3>
        </div>
        {action && <div className="flex-shrink-0">{action}</div>}
      </div>

      {/* Card body */}
      <div className="px-5 py-4">
        {loading ? (
          <div className="space-y-3 animate-pulse">
            <div className="h-4 w-3/4 rounded bg-gray-200" />
            <div className="h-4 w-1/2 rounded bg-gray-200" />
            <div className="h-4 w-5/6 rounded bg-gray-200" />
          </div>
        ) : (
          children
        )}
      </div>
    </div>
  );
};
