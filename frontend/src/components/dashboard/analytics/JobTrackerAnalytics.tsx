/**
 * JobTrackerAnalytics — visualizes the job application pipeline.
 *
 * Renders an animated donut chart of applications by status (from the
 * existing analytics endpoint), pipeline rate tiles (interview / offer /
 * rejection / success), and a monthly application bar chart. All values
 * come from the API — nothing is calculated here beyond presentation.
 *
 * @author DevLaunch
 */

import React, { useMemo } from 'react';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
} from 'recharts';
import { Briefcase, TrendingUp } from 'lucide-react';
import { AnalyticsCard } from './AnalyticsCard';
import { CountUp } from '../../ui/CountUp';
import { JOB_STATUS_COLORS, JOB_STATUS_ORDER } from './analyticsUtils';
import { APPLICATION_STATUS_LABELS } from '../../../types/job-application';
import type { DashboardResponse } from '../../../types/dashboard';
import type { ApplicationAnalytics } from '../../../types/job-application';

interface JobTrackerAnalyticsProps {
  /** The aggregated dashboard data (fallback counts). */
  data: DashboardResponse;
  /** The application analytics response, or null while loading/unavailable. */
  analytics: ApplicationAnalytics | null;
  /** Whether the analytics are still loading. */
  loading?: boolean;
}

/** Formats a "yyyy-MM" year-month into "Aug '26". */
function monthLabel(yearMonth: string): string {
  const [year, month] = yearMonth.split('-').map(Number);
  if (!year || !month) return yearMonth;
  const names = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  return `${names[month - 1]} '${String(year).slice(2)}`;
}

const RateTile: React.FC<{ label: string; value: number | null; tone: string }> = ({
  label,
  value,
  tone,
}) => (
  <div className="rounded-xl border border-gray-100 bg-gray-50/60 px-2.5 py-2 text-center">
    <p className={`text-lg font-bold ${tone}`}>
      {value != null ? <CountUp value={value} decimals={0} suffix="%" /> : '—'}
    </p>
    <p className="mt-0.5 text-[10px] font-medium uppercase tracking-wide text-gray-400">
      {label}
    </p>
  </div>
);

export const JobTrackerAnalytics: React.FC<JobTrackerAnalyticsProps> = ({
  data,
  analytics,
  loading = false,
}) => {
  const statusData = useMemo(() => {
    const counts = analytics?.statusCounts ?? {};
    return JOB_STATUS_ORDER.map((status) => ({
      name: APPLICATION_STATUS_LABELS[status],
      key: status,
      value: counts[status] ?? 0,
      color: JOB_STATUS_COLORS[status],
    })).filter((entry) => entry.value > 0);
  }, [analytics]);

  const total = analytics?.totalApplications ?? data.totalJobApplications;

  const monthlyData = useMemo(
    () =>
      (analytics?.monthlyApplications ?? []).map((m) => ({
        name: monthLabel(m.yearMonth),
        value: m.count,
      })),
    [analytics],
  );

  const rates = {
    interview: analytics?.interviewRate ?? null,
    offer: analytics?.offerRate ?? null,
    rejection: analytics?.rejectionRate ?? null,
    success: analytics?.successRate ?? null,
  };

  return (
    <AnalyticsCard
      title="Job Tracker Analytics"
      subtitle={`${total} ${total === 1 ? 'application' : 'applications'} in your pipeline`}
      icon={<Briefcase className="h-5 w-5" />}
      tone="orange"
      loading={loading}
    >
      {total === 0 ? (
        <div className="flex flex-col items-center py-8 text-center">
          <Briefcase className="mb-2 h-8 w-8 text-gray-300" />
          <p className="text-sm font-medium text-gray-500">No applications yet</p>
          <p className="mt-0.5 text-xs text-gray-400">
            Start tracking applications to visualize your pipeline
          </p>
        </div>
      ) : (
        <div>
          {/* Donut + legend */}
          {statusData.length > 0 ? (
            <div className="flex items-center gap-2">
              <div className="relative h-[150px] w-[150px] shrink-0">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={statusData}
                      cx="50%"
                      cy="50%"
                      innerRadius={48}
                      outerRadius={68}
                      dataKey="value"
                      nameKey="name"
                      paddingAngle={3}
                      strokeWidth={0}
                    >
                      {statusData.map((entry) => (
                        <Cell key={entry.key} fill={entry.color} />
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
                <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
                  <CountUp value={total} className="text-2xl font-bold text-gray-900" />
                  <span className="text-[10px] font-medium uppercase tracking-wide text-gray-400">
                    Total
                  </span>
                </div>
              </div>

              {/* Legend */}
              <ul className="min-w-0 flex-1 space-y-1.5">
                {statusData.map((entry) => {
                  const pct = Math.round((entry.value / total) * 100);
                  return (
                    <li key={entry.key} className="flex items-center gap-2 text-xs">
                      <span
                        className="h-2.5 w-2.5 shrink-0 rounded-full"
                        style={{ backgroundColor: entry.color }}
                      />
                      <span className="truncate text-gray-600">{entry.name}</span>
                      <span className="ml-auto font-semibold text-gray-800">{entry.value}</span>
                      <span className="w-9 text-right text-gray-400">{pct}%</span>
                    </li>
                  );
                })}
              </ul>
            </div>
          ) : (
            <p className="text-xs text-gray-400">No status data available.</p>
          )}

          {/* Rates */}
          <div className="mt-4 grid grid-cols-4 gap-2">
            <RateTile label="Interviews" value={rates.interview} tone="text-blue-600" />
            <RateTile label="Offers" value={rates.offer} tone="text-emerald-600" />
            <RateTile label="Rejected" value={rates.rejection} tone="text-red-500" />
            <RateTile label="Success" value={rates.success} tone="text-indigo-600" />
          </div>

          {/* Monthly applications */}
          {monthlyData.length > 0 && (
            <div className="mt-4">
              <p className="mb-1.5 flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-wide text-gray-400">
                <TrendingUp className="h-3 w-3" /> Monthly Applications
              </p>
              <ResponsiveContainer width="100%" height={100}>
                <BarChart data={monthlyData} margin={{ top: 4, right: 0, left: -26, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 10, fill: '#9ca3af' }}
                    axisLine={{ stroke: '#e5e7eb' }}
                    tickLine={false}
                    interval="preserveStartEnd"
                  />
                  <YAxis
                    allowDecimals={false}
                    tick={{ fontSize: 10, fill: '#9ca3af' }}
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
                  />
                  <Bar dataKey="value" fill="#f97316" radius={[3, 3, 0, 0]} maxBarSize={28} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      )}
    </AnalyticsCard>
  );
};
