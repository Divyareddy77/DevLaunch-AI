/**
 * PlacementAnalytics — visualizes the placement readiness score trend.
 *
 * Shows the previous vs. current score as an animated gradient area chart,
 * with a count-up current score, the net change since the previous
 * measurement, and the last-updated date. All values come straight from
 * the dashboard response — no scores are calculated here.
 *
 * @author DevLaunch
 */

import React, { useMemo } from 'react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { TrendingUp, TrendingDown, Minus, CalendarClock } from 'lucide-react';
import { AnalyticsCard } from './AnalyticsCard';
import { CountUp } from '../../ui/CountUp';
import { lastUpdatedLabel } from './analyticsUtils';
import type { DashboardResponse } from '../../../types/dashboard';

interface PlacementAnalyticsProps {
  /** The aggregated dashboard data. */
  data: DashboardResponse;
  /** Whether the dashboard data is still loading. */
  loading?: boolean;
}

/** Small labelled metric tile used in the summary row. */
const MetricTile: React.FC<{ label: string; value: string | number }> = ({ label, value }) => (
  <div className="rounded-xl border border-gray-100 bg-gray-50/60 px-3 py-2.5 text-center">
    <p className="text-sm font-bold text-gray-900">{value}</p>
    <p className="mt-0.5 text-[10px] font-medium uppercase tracking-wide text-gray-400">
      {label}
    </p>
  </div>
);

/** Pill badge showing the score change since the previous measurement. */
const ChangeBadge: React.FC<{ change: number | null }> = ({ change }) => {
  if (change === null) return null;
  if (change > 0) {
    return (
      <span className="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-bold text-emerald-700">
        <TrendingUp className="h-3.5 w-3.5" />+{change} pts vs previous
      </span>
    );
  }
  if (change < 0) {
    return (
      <span className="inline-flex items-center gap-1 rounded-full bg-red-100 px-2.5 py-1 text-xs font-bold text-red-700">
        <TrendingDown className="h-3.5 w-3.5" />
        {change} pts vs previous
      </span>
    );
  }
  return (
    <span className="inline-flex items-center gap-1 rounded-full bg-gray-100 px-2.5 py-1 text-xs font-bold text-gray-600">
      <Minus className="h-3.5 w-3.5" />No change
    </span>
  );
};

export const PlacementAnalytics: React.FC<PlacementAnalyticsProps> = ({
  data,
  loading = false,
}) => {
  const current = data.placementReadiness;
  const previous = data.readinessPrevious;
  const change = data.readinessChange;

  const hasPrevious = previous !== null && change !== null;

  const trendData = useMemo(() => {
    if (!hasPrevious) return [];
    return [
      { name: 'Previous', value: previous },
      { name: 'Current', value: current },
    ];
  }, [hasPrevious, previous, current]);

  return (
    <AnalyticsCard
      title="Placement Readiness Trend"
      subtitle="Score progression"
      icon={<TrendingUp className="h-5 w-5" />}
      tone="primary"
      loading={loading}
    >
      {/* Score summary */}
      <div className="flex items-end justify-between gap-3">
        <div>
          <div className="flex items-baseline gap-1">
            <CountUp value={current} className="text-4xl font-bold tracking-tight text-gray-900" />
            <span className="text-sm font-medium text-gray-400">/ 100</span>
          </div>
          <p className="mt-0.5 text-xs text-gray-400">Current readiness</p>
        </div>
        <ChangeBadge change={hasPrevious ? change : null} />
      </div>

      {/* Previous / change tiles */}
      <div className="mt-4 grid grid-cols-2 gap-2.5">
        <MetricTile label="Previous Score" value={hasPrevious ? previous : '—'} />
        <MetricTile label="Last Updated" value={lastUpdatedLabel(data.readinessUpdatedAt)} />
      </div>

      {/* Trend chart */}
      <div className="mt-4">
        {trendData.length === 0 ? (
          <div className="flex h-32 flex-col items-center justify-center rounded-xl border border-dashed border-gray-200 bg-gray-50/50 text-center">
            <TrendingUp className="mb-1.5 h-5 w-5 text-gray-300" />
            <p className="text-xs text-gray-400">
              Complete more activities to start tracking your trend
            </p>
          </div>
        ) : (
          <ResponsiveContainer width="100%" height={140}>
            <AreaChart
              data={trendData}
              margin={{ top: 8, right: 4, left: -18, bottom: 0 }}
            >
              <defs>
                <linearGradient id="placementGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#6366f1" stopOpacity={0.35} />
                  <stop offset="100%" stopColor="#6366f1" stopOpacity={0.02} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
              <XAxis
                dataKey="name"
                tick={{ fontSize: 11, fill: '#6b7280' }}
                axisLine={{ stroke: '#e5e7eb' }}
                tickLine={false}
              />
              <YAxis
                domain={[0, 100]}
                tick={{ fontSize: 11, fill: '#6b7280' }}
                axisLine={false}
                tickLine={false}
              />
              <Tooltip
                contentStyle={{
                  borderRadius: '10px',
                  border: '1px solid #e5e7eb',
                  boxShadow: '0 8px 24px -8px rgba(16,24,40,0.18)',
                  fontSize: '12px',
                }}
                formatter={(value: number) => [`${value} / 100`, 'Score']}
              />
              <Area
                type="monotone"
                dataKey="value"
                stroke="#6366f1"
                strokeWidth={2.5}
                fill="url(#placementGradient)"
                dot={{ r: 4, fill: '#6366f1', strokeWidth: 2, stroke: '#fff' }}
                activeDot={{ r: 6, fill: '#6366f1', strokeWidth: 2, stroke: '#fff' }}
              />
            </AreaChart>
          </ResponsiveContainer>
        )}
      </div>

      {hasPrevious && (
        <p className="mt-2 flex items-center gap-1.5 text-[11px] text-gray-400">
          <CalendarClock className="h-3 w-3" />
          {change !== null && change > 0
            ? `You're ${change} points closer to your goal`
            : `Progress tracked since ${lastUpdatedLabel(data.readinessUpdatedAt)}`}
        </p>
      )}
    </AnalyticsCard>
  );
};
