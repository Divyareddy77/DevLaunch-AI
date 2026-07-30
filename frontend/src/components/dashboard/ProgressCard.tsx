/**
 * ProgressCard — displays a single key metric with a label and
 * optional trend indicator.
 *
 * Used on the dashboard to show numerical summaries such as
 * "12 Resumes", "85% Complete", etc.
 *
 * @author DevLaunch
 */

import React, { type ReactNode } from 'react';

interface ProgressCardProps {
  /** Short label describing the metric (e.g. "Resumes Created"). */
  label: string;
  /** The primary numeric value to display. */
  value: string | number;
  /** Optional icon displayed above the value. */
  icon?: ReactNode;
  /** Optional trend direction indicator. */
  trend?: 'up' | 'down' | 'neutral';
  /** Optional trend label (e.g. "+12% from last month"). */
  trendLabel?: string;
  /** Optional background colour variant. */
  variant?: 'default' | 'primary' | 'success' | 'warning';
  /** Optional additional CSS classes. */
  className?: string;
}

const variantStyles: Record<string, string> = {
  default: 'bg-white border-gray-200',
  primary: 'bg-primary-50 border-primary-200',
  success: 'bg-emerald-50 border-emerald-200',
  warning: 'bg-amber-50 border-amber-200',
};

const trendIcons: Record<string, string> = {
  up: '↑',
  down: '↓',
  neutral: '→',
};

const trendColors: Record<string, string> = {
  up: 'text-emerald-600',
  down: 'text-red-600',
  neutral: 'text-gray-500',
};

export const ProgressCard: React.FC<ProgressCardProps> = ({
  label,
  value,
  icon,
  trend,
  trendLabel,
  variant = 'default',
  className = '',
}) => {
  return (
    <div
      className={`rounded-xl border p-4 shadow-sm transition-shadow hover:shadow-md ${variantStyles[variant]} ${className}`}
    >
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-xs font-medium uppercase tracking-wider text-gray-500">{label}</p>
          <p className="mt-1 text-2xl font-bold text-gray-900">{value}</p>

          {trend && trendLabel && (
            <p className={`mt-1 text-xs font-medium ${trendColors[trend]}`}>
              <span className="mr-0.5">{trendIcons[trend]}</span>
              {trendLabel}
            </p>
          )}
        </div>

        {icon && (
          <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg bg-gray-100 text-gray-600">
            {icon}
          </div>
        )}
      </div>
    </div>
  );
};
