/**
 * LeetCodeAnalytics — visualizes LeetCode progress.
 *
 * Renders the solved-count with an animated difficulty donut, a stacked
 * difficulty bar, and an animated acceptance-rate ring. All values come
 * directly from the existing dashboard/LeetCode responses.
 *
 * @author DevLaunch
 */

import React, { useMemo } from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts';
import { Code2, Trophy, Link2, RefreshCw } from 'lucide-react';
import { AnalyticsCard } from './AnalyticsCard';
import { CountUp } from '../../ui/CountUp';
import { useCountUp } from '../../../hooks/useCountUp';
import type { DashboardResponse } from '../../../types/dashboard';

interface LeetCodeAnalyticsProps {
  /** The aggregated dashboard data. */
  data: DashboardResponse;
  /** Whether the data is still loading. */
  loading?: boolean;
}

const DIFFICULTY_COLORS = {
  Easy: '#10b981',
  Medium: '#f59e0b',
  Hard: '#ef4444',
} as const;

/** Small animated circular progress ring used for the acceptance rate. */
const AcceptanceRing: React.FC<{ value: number | null }> = ({ value }) => {
  const animated = useCountUp(value ?? 0, 1000);
  const size = 74;
  const strokeWidth = 8;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (animated / 100) * circumference;

  return (
    <div className="relative inline-flex items-center justify-center">
      <svg width={size} height={size} className="-rotate-90">
        <circle cx={size / 2} cy={size / 2} r={radius} fill="none" stroke="#eef0f4" strokeWidth={strokeWidth} />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="#ef4444"
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
        />
      </svg>
      <div className="absolute flex flex-col items-center">
        <span className="text-lg font-bold text-gray-900">{Math.round(animated)}%</span>
      </div>
    </div>
  );
};

export const LeetCodeAnalytics: React.FC<LeetCodeAnalyticsProps> = ({
  data,
  loading = false,
}) => {
  const connected = data.leetcodeUsername !== null;

  const difficultyData = useMemo(() => {
    const entries = [
      { name: 'Easy', value: data.leetcodeEasySolved ?? 0, color: DIFFICULTY_COLORS.Easy },
      { name: 'Medium', value: data.leetcodeMediumSolved ?? 0, color: DIFFICULTY_COLORS.Medium },
      { name: 'Hard', value: data.leetcodeHardSolved ?? 0, color: DIFFICULTY_COLORS.Hard },
    ];
    return entries.filter((entry) => entry.value > 0);
  }, [data]);

  const total = data.leetcodeSolved;
  const stacked = difficultyData.map((entry) => ({
    ...entry,
    pct: total > 0 ? Math.round((entry.value / total) * 100) : 0,
  }));

  return (
    <AnalyticsCard
      title="LeetCode Analytics"
      subtitle={connected ? `@${data.leetcodeUsername}` : 'Connect to unlock analytics'}
      icon={<Code2 className="h-5 w-5" />}
      tone="danger"
      loading={loading}
    >
      {!connected ? (
        <div className="flex flex-col items-center py-8 text-center">
          <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-red-50">
            <Code2 className="h-6 w-6 text-red-400" />
          </div>
          <p className="text-sm font-medium text-gray-700">No LeetCode account connected</p>
          <p className="mt-1 max-w-[220px] text-xs leading-relaxed text-gray-400">
            Connect your account to visualize your problem-solving progress.
          </p>
          <span className="mt-3 inline-flex items-center gap-1.5 text-xs font-medium text-red-500">
            <Link2 className="h-3.5 w-3.5" /> Connect from the LeetCode page
          </span>
        </div>
      ) : (
        <div>
          {/* Headline */}
          <div className="flex items-center justify-between">
            <div>
              <div className="flex items-baseline gap-1">
                <CountUp value={total} className="text-3xl font-bold tracking-tight text-gray-900" />
                <span className="text-xs text-gray-400">solved</span>
              </div>
              <p className="text-[11px] text-gray-400">
                {data.leetcodeEasySolved ?? 0}E · {data.leetcodeMediumSolved ?? 0}M ·{' '}
                {data.leetcodeHardSolved ?? 0}H
              </p>
            </div>
            {data.leetcodeRanking != null && (
              <span className="inline-flex items-center gap-1 rounded-full bg-red-50 px-2.5 py-1 text-xs font-semibold text-red-600">
                <Trophy className="h-3 w-3" />
                #{data.leetcodeRanking.toLocaleString()}
              </span>
            )}
          </div>

          {/* Donut + legend */}
          {difficultyData.length > 0 && (
            <div className="mt-3 flex items-center gap-3">
              <div className="h-[120px] w-[120px] shrink-0">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={difficultyData}
                      cx="50%"
                      cy="50%"
                      innerRadius={38}
                      outerRadius={56}
                      dataKey="value"
                      nameKey="name"
                      paddingAngle={3}
                      strokeWidth={0}
                    >
                      {difficultyData.map((entry) => (
                        <Cell key={entry.name} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{
                        borderRadius: '10px',
                        border: '1px solid #e5e7eb',
                        boxShadow: '0 8px 24px -8px rgba(16,24,40,0.18)',
                        fontSize: '12px',
                      }}
                      formatter={(value: number, name: string) => [`${value}`, name]}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>

              <ul className="min-w-0 flex-1 space-y-1.5">
                {stacked.map((entry) => (
                  <li key={entry.name} className="flex items-center gap-2 text-xs">
                    <span className="h-2.5 w-2.5 shrink-0 rounded-full" style={{ backgroundColor: entry.color }} />
                    <span className="text-gray-600">{entry.name}</span>
                    <span className="ml-auto font-semibold text-gray-800">{entry.value}</span>
                    <span className="w-9 text-right text-gray-400">{entry.pct}%</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Acceptance ring + stacked bar */}
          <div className="mt-4 flex items-center gap-4">
            <div className="flex flex-col items-center">
              <AcceptanceRing value={data.leetcodeAcceptanceRate} />
              <span className="mt-1 text-[10px] font-medium uppercase tracking-wide text-gray-400">
                Acceptance
              </span>
            </div>
            <div className="min-w-0 flex-1">
              <p className="mb-1.5 text-[11px] font-semibold uppercase tracking-wide text-gray-400">
                Difficulty Mix
              </p>
              <div className="flex h-3 w-full overflow-hidden rounded-full bg-gray-100">
                {stacked.map((entry) => (
                  <div
                    key={entry.name}
                    className="h-full transition-all duration-1000"
                    style={{ width: `${entry.pct}%`, backgroundColor: entry.color }}
                  />
                ))}
              </div>
              <p className="mt-2 flex items-center gap-1.5 text-[11px] text-gray-400">
                <RefreshCw className="h-3 w-3" />
                Live data synced from LeetCode
              </p>
            </div>
          </div>
        </div>
      )}
    </AnalyticsCard>
  );
};
