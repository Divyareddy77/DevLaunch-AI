/**
 * DashboardCard — a generic card wrapper for dashboard widget sections.
 *
 * Provides consistent padding, title, icon, and an optional action area
 * so every dashboard widget shares the same base styling. Cards use soft
 * shadows, rounded corners, a coloured icon chip, and a subtle hover lift
 * for a premium SaaS look.
 *
 * @author DevLaunch
 */

import React, { type ReactNode } from 'react';
import { CARD_TONES, type CardTone } from './cardTones';

interface DashboardCardProps {
  /** The card title displayed at the top. */
  title: string;
  /** Optional icon rendered to the left of the title. */
  icon?: ReactNode;
  /** Optional action element placed on the right side of the header (e.g. a link or button). */
  action?: ReactNode;
  /** Semantic tone controlling the icon chip colour. */
  tone?: CardTone;
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
  tone = 'primary',
  loading = false,
  children,
  className = '',
}) => {
  const t = CARD_TONES[tone];

  return (
    <div
      className={`group relative flex h-full flex-col overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-[0_1px_3px_rgba(16,24,40,0.06)] transition-all duration-300 hover:-translate-y-1 hover:border-gray-200 hover:shadow-[0_16px_40px_-12px_rgba(16,24,40,0.16)] ${className}`}
    >
      {/* Card header */}
      <div className="flex items-center justify-between border-b border-gray-100 px-5 py-4">
        <div className="flex items-center gap-2.5">
          {icon && (
            <span
              className={`flex h-9 w-9 items-center justify-center rounded-xl transition-transform duration-300 group-hover:scale-105 ${t.chip}`}
            >
              {icon}
            </span>
          )}
          <h3 className="text-sm font-semibold text-gray-800">{title}</h3>
        </div>
        {action && <div className="flex-shrink-0">{action}</div>}
      </div>

      {/* Card body */}
      <div className="flex-1 px-5 py-4">
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
