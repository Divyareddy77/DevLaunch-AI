/**
 * StatsCard — displays a single statistic with an icon,
 * value, label, and optional trend arrow.
 *
 * Slightly larger than ProgressCard and intended as a
 * hero statistic on the dashboard (placement readiness score,
 * total applications, etc.).
 *
 * @author DevLaunch
 */

import React, { type ReactNode } from 'react';
import { TrendingUp, TrendingDown, Minus } from 'lucide-react';

interface StatsCardProps {
  /** The numeric or string value to highlight. */
  value: string | number;
  /** Label describing what this statistic represents. */
  label: string;
  /** Optional icon rendered above the value. */
  icon?: ReactNode;
  /** Optional trend direction. */
  trend?: 'up' | 'down' | 'flat';
  /** Optional percentage change to display. */
  change?: string;
  /** Optional colour variant. */
  color?: 'indigo' | 'emerald' | 'amber' | 'rose' | 'blue' | 'purple';
  /** Optional additional CSS classes. */
  className?: string;
}

const colorMap: Record<string, { bg: string; iconBg: string; text: string }> = {
  indigo: { bg: 'bg-indigo-50', iconBg: 'bg-indigo-100', text: 'text-indigo-600' },
  emerald: { bg: 'bg-emerald-50', iconBg: 'bg-emerald-100', text: 'text-emerald-600' },
  amber: { bg: 'bg-amber-50', iconBg: 'bg-amber-100', text: 'text-amber-600' },
  rose: { bg: 'bg-rose-50', iconBg: 'bg-rose-100', text: 'text-rose-600' },
  blue: { bg: 'bg-blue-50', iconBg: 'bg-blue-100', text: 'text-blue-600' },
  purple: { bg: 'bg-purple-50', iconBg: 'bg-purple-100', text: 'text-purple-600' },
};

const TrendIcon: React.FC<{ trend: 'up' | 'down' | 'flat' }> = ({ trend }) => {
  if (trend === 'up') return <TrendingUp className="h-4 w-4 text-emerald-500" />;
  if (trend === 'down') return <TrendingDown className="h-4 w-4 text-red-500" />;
  return <Minus className="h-4 w-4 text-gray-400" />;
};

export const StatsCard: React.FC<StatsCardProps> = ({
  value,
  label,
  icon,
  trend,
  change,
  color = 'indigo',
  className = '',
}) => {
  const colors = colorMap[color];

  return (
    <div className={`rounded-xl border border-gray-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md ${className}`}>
      <div className="flex items-center gap-4">
        {icon && (
          <div className={`flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-xl ${colors.iconBg} ${colors.text}`}>
            {icon}
          </div>
        )}

        <div className="flex-1 min-w-0">
          <p className="text-2xl font-bold text-gray-900">{value}</p>
          <p className="mt-0.5 text-sm text-gray-500 truncate">{label}</p>

          {trend && change && (
            <div className="mt-1 flex items-center gap-1">
              <TrendIcon trend={trend} />
              <span className="text-xs font-medium text-gray-500">{change}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
