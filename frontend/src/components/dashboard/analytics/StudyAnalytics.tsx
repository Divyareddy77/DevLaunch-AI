/**
 * StudyAnalytics — visualizes study plan progress.
 *
 * Derives completed/pending task counts, the current study streak, weekly
 * study hours (from scheduled session times), a 7-day calendar grid, and
 * monthly progress from the existing study-planner response. Pure
 * presentation — every value originates from the API.
 *
 * @author DevLaunch
 */

import React, { useMemo } from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { parseISO } from 'date-fns';
import { CalendarCheck, Flame, CheckCircle2, Clock3 } from 'lucide-react';
import { AnalyticsCard } from './AnalyticsCard';
import { CountUp } from '../../ui/CountUp';
import {
  countStreak,
  hoursBetween,
  lastNDays,
  matchesDateKey,
  shortDayLabel,
  toDateKey,
} from './analyticsUtils';
import type { DashboardResponse } from '../../../types/dashboard';
import type { StudyPlannerResponse } from '../../../types/study-planner';

interface StudyAnalyticsProps {
  /** The aggregated dashboard data (fallback counts). */
  data: DashboardResponse;
  /** All study planner tasks, or null while loading/unavailable. */
  tasks: StudyPlannerResponse[] | null;
  /** Whether the tasks are still loading. */
  loading?: boolean;
}

const StatTile: React.FC<{ label: string; value: React.ReactNode; icon?: React.ReactNode }> = ({
  label,
  value,
  icon,
}) => (
  <div className="rounded-xl border border-gray-100 bg-gray-50/60 px-2.5 py-2 text-center">
    <p className="flex items-center justify-center gap-1 text-lg font-bold text-gray-900">
      {icon}
      {value}
    </p>
    <p className="mt-0.5 text-[10px] font-medium uppercase tracking-wide text-gray-400">
      {label}
    </p>
  </div>
);

export const StudyAnalytics: React.FC<StudyAnalyticsProps> = ({
  data,
  tasks,
  loading = false,
}) => {
  const derived = useMemo(() => {
    const list = tasks ?? [];
    const completed = list.filter((t) => t.status === 'COMPLETED').length;
    const total = list.length;
    const rate = total > 0 ? Math.round((completed / total) * 100) : 0;

    // Parse date-only strings with date-fns so day keys stay in local time
    // (raw `new Date('yyyy-MM-dd')` parses as UTC and can shift a day).
    const completedDayKeys = new Set(
      list
        .filter((t) => t.status === 'COMPLETED')
        .map((t) => {
          const parsed = parseISO(t.studyDate);
          return Number.isNaN(parsed.getTime()) ? '' : toDateKey(parsed);
        })
        .filter((key) => key !== ''),
    );
    const streak = countStreak(completedDayKeys);

    const days = lastNDays(7);
    const week = days.map((day) => {
      const dayTasks = list.filter((t) => matchesDateKey(day, t.studyDate));
      const dayCompleted = dayTasks.filter((t) => t.status === 'COMPLETED').length;
      return {
        day,
        label: shortDayLabel(day),
        date: day.getDate(),
        count: dayTasks.length,
        completed: dayCompleted,
        hours: dayTasks.reduce((sum, t) => sum + hoursBetween(t.startTime, t.endTime), 0),
      };
    });

    const now = new Date();
    const monthKey = toDateKey(now).slice(0, 7);
    const monthTasks = list.filter((t) => t.studyDate.startsWith(monthKey));
    const monthCompleted = monthTasks.filter((t) => t.status === 'COMPLETED').length;
    const monthRate = monthTasks.length > 0 ? Math.round((monthCompleted / monthTasks.length) * 100) : 0;

    const totalHours = week.reduce((sum, d) => sum + d.hours, 0);

    return { completed, total, rate, streak, week, totalHours, monthTasks, monthCompleted, monthRate };
  }, [tasks]);

  const totalTasks = derived.total > 0 ? derived.total : data.totalStudyTasks;
  const completedTasks = derived.total > 0 ? derived.completed : data.completedStudyTasks;
  const rate = derived.total > 0 ? derived.rate : (totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0);

  const barData = derived.week.map((d) => ({
    name: d.label,
    hours: Math.round(d.hours * 10) / 10,
  }));

  const hasTasks = totalTasks > 0;

  return (
    <AnalyticsCard
      title="Study Analytics"
      subtitle={`${rate}% completion rate`}
      icon={<CalendarCheck className="h-5 w-5" />}
      tone="warning"
      loading={loading}
    >
      {!hasTasks ? (
        <div className="flex flex-col items-center py-8 text-center">
          <CalendarCheck className="mb-2 h-8 w-8 text-gray-300" />
          <p className="text-sm font-medium text-gray-500">No study sessions yet</p>
          <p className="mt-0.5 text-xs text-gray-400">
            Plan your first session to unlock weekly analytics
          </p>
        </div>
      ) : (
        <div>
          {/* Headline stats */}
          <div className="grid grid-cols-3 gap-2.5">
            <StatTile
              label="Completed"
              value={<CountUp value={completedTasks} />}
              icon={<CheckCircle2 className="h-4 w-4 text-emerald-500" />}
            />
            <StatTile
              label="Pending"
              value={<CountUp value={Math.max(0, totalTasks - completedTasks)} />}
            />
            <StatTile
              label="Streak"
              value={
                <span className="flex items-center gap-1">
                  <CountUp value={derived.streak} />
                  <Flame className="h-4 w-4 text-orange-500" />
                </span>
              }
            />
          </div>

          {/* Weekly hours */}
          {derived.totalHours > 0 ? (
            <div className="mt-4">
              <p className="mb-1.5 flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-wide text-gray-400">
                <Clock3 className="h-3 w-3" /> Weekly Study Hours
                <span className="ml-auto font-normal normal-case text-gray-500">
                  {Math.round(derived.totalHours * 10) / 10}h total
                </span>
              </p>
              <ResponsiveContainer width="100%" height={100}>
                <BarChart data={barData} margin={{ top: 4, right: 0, left: -26, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 10, fill: '#9ca3af' }}
                    axisLine={{ stroke: '#e5e7eb' }}
                    tickLine={false}
                  />
                  <YAxis
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
                    formatter={(value: number) => [`${value}h`, 'Hours']}
                  />
                  <Bar dataKey="hours" fill="#f59e0b" radius={[3, 3, 0, 0]} maxBarSize={24} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="mt-4 flex h-20 items-center justify-center rounded-xl border border-dashed border-gray-200 bg-gray-50/50">
              <p className="text-xs text-gray-400">No study hours scheduled this week</p>
            </div>
          )}

          {/* Weekly calendar grid */}
          <div className="mt-4">
            <p className="mb-1.5 text-[11px] font-semibold uppercase tracking-wide text-gray-400">
              This Week
            </p>
            <div className="grid grid-cols-7 gap-1.5">
              {derived.week.map((d) => {
                const hasActivity = d.count > 0;
                const allDone = hasActivity && d.completed === d.count;
                return (
                  <div
                    key={toDateKey(d.day)}
                    title={`${d.label} — ${d.completed}/${d.count} tasks`}
                    className={`flex flex-col items-center rounded-lg border px-1 py-1.5 text-center transition-colors ${
                      allDone
                        ? 'border-emerald-100 bg-emerald-50'
                        : hasActivity
                          ? 'border-amber-100 bg-amber-50/70'
                          : 'border-gray-100 bg-gray-50/50'
                    }`}
                  >
                    <span className="text-[9px] font-semibold uppercase text-gray-400">
                      {d.label}
                    </span>
                    <span className="text-sm font-bold text-gray-800">{d.date}</span>
                    {hasActivity ? (
                      <span
                        className={`mt-0.5 rounded-full px-1 text-[9px] font-bold ${
                          allDone ? 'bg-emerald-200 text-emerald-800' : 'bg-amber-200 text-amber-800'
                        }`}
                      >
                        {d.completed}/{d.count}
                      </span>
                    ) : (
                      <span className="mt-0.5 text-[9px] text-gray-300">·</span>
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Monthly progress */}
          {derived.monthTasks.length > 0 && (
            <div className="mt-4">
              <div className="flex items-center justify-between text-[11px] text-gray-400">
                <span>Monthly progress</span>
                <span className="font-semibold text-gray-700">
                  {derived.monthCompleted}/{derived.monthTasks.length} tasks · {derived.monthRate}%
                </span>
              </div>
              <div className="mt-1 h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
                <div
                  className="h-full rounded-full bg-gradient-to-r from-amber-400 to-emerald-500 transition-all duration-1000"
                  style={{ width: `${derived.monthRate}%` }}
                />
              </div>
            </div>
          )}
        </div>
      )}
    </AnalyticsCard>
  );
};
