/**
 * AnalyticsCard — the shared card shell for the Analytics & Insights
 * section. Matches the premium dashboard card language (soft shadow,
 * hover lift, tone-coloured icon chip) and renders a chart-shaped
 * skeleton while its data is loading.
 *
 * @author DevLaunch
 */

import React, { type ReactNode } from 'react';
import { CARD_TONES, type CardTone } from '../cardTones';

interface AnalyticsCardProps {
  /** Card title. */
  title: string;
  /** Small supporting line under the title. */
  subtitle?: string;
  /** Icon rendered in the header chip. */
  icon: ReactNode;
  /** Semantic tone controlling the icon chip. */
  tone?: CardTone;
  /** Optional right-side action (e.g. a link). */
  action?: ReactNode;
  /** Whether the card content is still loading (shows a skeleton). */
  loading?: boolean;
  /** Main content. */
  children: ReactNode;
  /** Optional additional CSS classes. */
  className?: string;
}

/** Chart-shaped skeleton shown while data is loading. */
export const AnalyticsSkeleton: React.FC = () => (
  <div className="animate-pulse">
    <div className="flex items-end justify-between gap-4">
      <div className="space-y-2">
        <div className="h-7 w-16 rounded bg-gray-200" />
        <div className="h-3 w-24 rounded bg-gray-200" />
      </div>
      <div className="h-10 w-10 rounded-full bg-gray-200" />
    </div>
    <div className="mt-4 flex h-28 items-end gap-1.5">
      {Array.from({ length: 8 }).map((_, i) => (
        <div
          key={i}
          className="flex-1 rounded-t bg-gray-200"
          style={{ height: `${28 + ((i * 37) % 60)}%` }}
        />
      ))}
    </div>
    <div className="mt-4 h-2 rounded-full bg-gray-200" />
    <div className="mt-2 h-2 w-2/3 rounded-full bg-gray-200" />
  </div>
);

export const AnalyticsCard: React.FC<AnalyticsCardProps> = ({
  title,
  subtitle,
  icon,
  tone = 'primary',
  action,
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
          <span
            className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-xl transition-transform duration-300 group-hover:scale-105 ${t.chip}`}
          >
            {icon}
          </span>
          <div className="min-w-0">
            <h3 className="truncate text-sm font-semibold text-gray-800">{title}</h3>
            {subtitle && <p className="truncate text-[11px] text-gray-400">{subtitle}</p>}
          </div>
        </div>
        {action && <div className="flex-shrink-0">{action}</div>}
      </div>

      {/* Card body */}
      <div className="flex-1 px-5 py-4">
        {loading ? <AnalyticsSkeleton /> : children}
      </div>
    </div>
  );
};
