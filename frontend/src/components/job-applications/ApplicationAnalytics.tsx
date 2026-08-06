/**
 * ApplicationAnalytics — richer analytics for the job tracker.
 *
 * Replaces the simple counters with meaningful funnel metrics: applications
 * by status (pie), applications per month (bar), interview/offer/rejection/
 * success rates, average response time, and active/upcoming interviews.
 * Reuses the existing chart components — no new charting dependency.
 *
 * @author DevLaunch
 */

import React, { useMemo } from 'react';
import {
  Target,
  Trophy,
  XCircle,
  Timer,
  CalendarClock,
  Activity,
  CalendarDays,
  TrendingUp,
} from 'lucide-react';
import { PieChart, BarChart } from '../charts';
import { formatDate, formatTime } from '../../utils/date';
import {
  APPLICATION_STATUSES,
  APPLICATION_STATUS_LABELS,
  type ApplicationAnalytics as ApplicationAnalyticsData,
  type ApplicationStatusEnum,
} from '../../types/job-application';

interface ApplicationAnalyticsProps {
  /** The analytics data to display. */
  analytics: ApplicationAnalyticsData;
}

const statusColors: Record<ApplicationStatusEnum, string> = {
  WISHLIST: '#9ca3af',
  APPLIED: '#6366f1',
  ASSESSMENT: '#f59e0b',
  INTERVIEW: '#3b82f6',
  OFFER: '#10b981',
  REJECTED: '#ef4444',
};

function rateLabel(value: number | null): string {
  return value === null ? '—' : `${value}%`;
}

export const ApplicationAnalytics: React.FC<ApplicationAnalyticsProps> = ({ analytics }) => {
  const pieData = useMemo(
    () =>
      APPLICATION_STATUSES.filter((status) => (analytics.statusCounts[status] ?? 0) > 0).map(
        (status) => ({
          name: APPLICATION_STATUS_LABELS[status],
          value: analytics.statusCounts[status] ?? 0,
          color: statusColors[status],
        }),
      ),
    [analytics.statusCounts],
  );

  const barData = useMemo(
    () =>
      analytics.monthlyApplications.map((month) => ({
        name: month.yearMonth,
        value: month.count,
      })),
    [analytics.monthlyApplications],
  );

  const statCards = [
    {
      label: 'Interview Rate',
      value: rateLabel(analytics.interviewRate),
      icon: <Activity className="h-4 w-4" />,
      accent: 'bg-blue-50 text-blue-600',
    },
    {
      label: 'Offer Rate',
      value: rateLabel(analytics.offerRate),
      icon: <Trophy className="h-4 w-4" />,
      accent: 'bg-emerald-50 text-emerald-600',
    },
    {
      label: 'Rejection Rate',
      value: rateLabel(analytics.rejectionRate),
      icon: <XCircle className="h-4 w-4" />,
      accent: 'bg-red-50 text-red-600',
    },
    {
      label: 'Success Rate',
      value: rateLabel(analytics.successRate),
      icon: <TrendingUp className="h-4 w-4" />,
      accent: 'bg-violet-50 text-violet-600',
    },
    {
      label: 'Avg. Response Time',
      value: analytics.averageResponseTimeDays === null ? '—' : `${analytics.averageResponseTimeDays}d`,
      icon: <Timer className="h-4 w-4" />,
      accent: 'bg-amber-50 text-amber-600',
    },
    {
      label: 'Active Interviews',
      value: String(analytics.activeInterviews),
      icon: <Target className="h-4 w-4" />,
      accent: 'bg-cyan-50 text-cyan-600',
    },
  ];

  return (
    <div className="rounded-xl border border-gray-200 bg-white p-5">
      {/* Stat cards */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
        {statCards.map((stat) => (
          <div key={stat.label} className="rounded-xl border border-gray-100 bg-gray-50/60 p-3">
            <span className={`inline-flex h-7 w-7 items-center justify-center rounded-lg ${stat.accent}`}>
              {stat.icon}
            </span>
            <p className="mt-2 text-xl font-bold text-gray-900">{stat.value}</p>
            <p className="text-[11px] font-medium text-gray-500">{stat.label}</p>
          </div>
        ))}
      </div>

      {/* Charts */}
      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <div className="rounded-xl border border-gray-100 p-4">
          <PieChart data={pieData} title="Applications by Status" innerRadius={50} />
        </div>
        <div className="rounded-xl border border-gray-100 p-4">
          <BarChart data={barData} title="Applications per Month" barColor="#6366f1" />
        </div>
      </div>

      {/* Upcoming interviews */}
      <div className="mt-6">
        <h4 className="mb-3 flex items-center gap-1.5 text-sm font-semibold text-gray-700">
          <CalendarDays className="h-4 w-4 text-indigo-500" />
          Upcoming Interviews
          {analytics.upcomingInterviews.length > 0 && (
            <span className="rounded-full bg-indigo-100 px-2 py-0.5 text-[10px] font-semibold text-indigo-600">
              {analytics.upcomingInterviews.length}
            </span>
          )}
        </h4>
        {analytics.upcomingInterviews.length === 0 ? (
          <p className="rounded-lg border border-dashed border-gray-200 py-6 text-center text-xs text-gray-400">
            No upcoming interviews scheduled.
          </p>
        ) : (
          <ul className="space-y-2">
            {analytics.upcomingInterviews.map((interview) => (
              <li
                key={interview.id}
                className="flex items-center justify-between gap-3 rounded-xl border border-gray-100 bg-gray-50/60 px-4 py-2.5"
              >
                <div className="flex items-center gap-2.5 min-w-0">
                  <span className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-blue-100 text-blue-600">
                    <CalendarClock className="h-4 w-4" />
                  </span>
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium text-gray-900">
                      {interview.title}
                      {interview.round ? ` · ${interview.round}` : ''}
                    </p>
                    <p className="truncate text-[11px] text-gray-400">
                      {formatDate(interview.scheduledDate)}
                      {interview.scheduledTime ? ` at ${formatTime(interview.scheduledTime)}` : ''}
                      {interview.interviewer ? ` · ${interview.interviewer}` : ''}
                    </p>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};
