/**
 * InterviewAnalytics — visualizes the mock interview performance trend.
 *
 * Derives per-session score, confidence, communication, and speaking-pace
 * series from the existing interview-history response and renders them as
 * animated line charts with supporting stat tiles. Pure presentation —
 * every value originates from the API.
 *
 * @author DevLaunch
 */

import React, { useMemo } from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { Mic, TrendingUp, TrendingDown, Gauge, CheckCircle2 } from 'lucide-react';
import { AnalyticsCard } from './AnalyticsCard';
import { CountUp } from '../../ui/CountUp';
import { safeParseDate, shortDateLabel } from './analyticsUtils';
import type { DashboardResponse } from '../../../types/dashboard';
import type { InterviewHistoryResponse } from '../../../types/ai';

interface InterviewAnalyticsProps {
  /** The aggregated dashboard data (fallback stats + insight). */
  data: DashboardResponse;
  /** The interview history response, or null while loading/unavailable. */
  history: InterviewHistoryResponse | null;
  /** Whether the history is still loading. */
  loading?: boolean;
}

/** Small stat tile with an optional trend arrow and subtext. */
const StatTile: React.FC<{
  label: string;
  value: React.ReactNode;
  delta?: number | null;
  subtext?: string;
}> = ({ label, value, delta, subtext }) => (
  <div className="rounded-xl border border-gray-100 bg-gray-50/60 px-2.5 py-2 text-center">
    <p className="text-lg font-bold text-gray-900">{value}</p>
    <p className="mt-0.5 text-[10px] font-medium uppercase tracking-wide text-gray-400">
      {label}
    </p>
    {delta !== undefined && delta !== null && delta !== 0 && (
      <p
        className={`mt-0.5 inline-flex items-center gap-0.5 text-[10px] font-bold ${
          delta > 0 ? 'text-emerald-600' : 'text-red-500'
        }`}
      >
        {delta > 0 ? <TrendingUp className="h-3 w-3" /> : <TrendingDown className="h-3 w-3" />}
        {delta > 0 ? '+' : ''}
        {delta}
      </p>
    )}
    {subtext && <p className="mt-0.5 text-[9px] text-gray-400">{subtext}</p>}
  </div>
);

/** Shared tooltip style for the interview charts. */
const CHART_TOOLTIP_STYLE = {
  borderRadius: '10px',
  border: '1px solid #e5e7eb',
  boxShadow: '0 8px 24px -8px rgba(16,24,40,0.18)',
  fontSize: '12px',
} as const;

export const InterviewAnalytics: React.FC<InterviewAnalyticsProps> = ({
  data,
  history,
  loading = false,
}) => {
  const items = useMemo(() => {
    const list = [...(history?.history ?? [])];
    return list.sort((a, b) => a.completedAt.localeCompare(b.completedAt));
  }, [history]);

  const stats = useMemo(() => {
    const total = history?.totalInterviews ?? data.mockInterviewCount;
    const average = history?.averageScore ?? data.mockInterviewAverageScore;
    const best = history?.bestScore ?? data.mockInterviewBestScore;
    const successRate = history?.successRate ?? null;

    // Speaking pace: words per minute per session.
    const paces = items
      .map((item) =>
        item.wordCount && item.durationSeconds && item.durationSeconds > 0
          ? item.wordCount / (item.durationSeconds / 60)
          : null,
      )
      .filter((p): p is number => p !== null);
    const latestPace = paces.length > 0 ? paces[paces.length - 1] : null;
    const prevPace = paces.length > 1 ? paces[paces.length - 2] : null;
    const avgPace =
      paces.length > 0
        ? paces.reduce((sum, p) => sum + p, 0) / paces.length
        : null;
    const paceDelta =
      latestPace !== null && prevPace !== null
        ? Math.round((latestPace - prevPace) * 10) / 10
        : null;

    const last = items[items.length - 1];
    const secondLast = items[items.length - 2];
    const latestConfidence = last?.confidenceScore ?? null;
    const latestCommunication = last?.communicationScore ?? null;

    const trend = items.map((item) => ({
      name: shortDateLabel(safeParseDate(item.completedAt) ?? new Date()),
      score: item.overallScore,
      confidence: item.confidenceScore ?? null,
      communication: item.communicationScore ?? null,
    }));

    // Cumulative interview completions — a real completion trend.
    const completionTrend = items.map((item, index) => ({
      name: shortDateLabel(safeParseDate(item.completedAt) ?? new Date()),
      completed: index + 1,
    }));

    return {
      total,
      average,
      best,
      successRate,
      latestPace,
      avgPace,
      paceDelta,
      latestConfidence,
      latestCommunication,
      confidenceDelta:
        latestConfidence !== null && secondLast?.confidenceScore != null
          ? latestConfidence - secondLast.confidenceScore
          : null,
      communicationDelta:
        latestCommunication !== null && secondLast?.communicationScore != null
          ? latestCommunication - secondLast.communicationScore
          : null,
      trend,
      completionTrend,
    };
  }, [items, history, data]);

  const hasInterviews = items.length > 0 || data.mockInterviewCount > 0;

  return (
    <AnalyticsCard
      title="Interview Analytics"
      subtitle={`${data.mockInterviewCount} ${data.mockInterviewCount === 1 ? 'session' : 'sessions'} completed`}
      icon={<Mic className="h-5 w-5" />}
      tone="violet"
      loading={loading}
    >
      {!hasInterviews ? (
        <div className="flex flex-col items-center py-8 text-center">
          <Mic className="mb-2 h-8 w-8 text-gray-300" />
          <p className="text-sm font-medium text-gray-500">No interviews yet</p>
          <p className="mt-0.5 text-xs text-gray-400">
            Complete a mock interview to unlock your performance trends
          </p>
        </div>
      ) : (
        <div>
          {/* Headline stats */}
          <div className="grid grid-cols-3 gap-2.5">
            <StatTile label="Total" value={<CountUp value={stats.total} />} />
            <StatTile
              label="Average"
              value={stats.average != null ? <CountUp value={stats.average} decimals={0} /> : '—'}
            />
            <StatTile label="Best" value={stats.best ?? '—'} />
          </div>

          {/* Score trend */}
          {stats.trend.length > 1 ? (
            <div className="mt-4">
              <p className="mb-1.5 text-[11px] font-semibold uppercase tracking-wide text-gray-400">
                Score Trend
              </p>
              <ResponsiveContainer width="100%" height={120}>
                <LineChart
                  data={stats.trend}
                  margin={{ top: 6, right: 4, left: -22, bottom: 0 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 10, fill: '#9ca3af' }}
                    axisLine={{ stroke: '#e5e7eb' }}
                    tickLine={false}
                    interval="preserveStartEnd"
                  />
                  <YAxis
                    domain={[0, 100]}
                    tick={{ fontSize: 10, fill: '#9ca3af' }}
                    axisLine={false}
                    tickLine={false}
                  />
                  <Tooltip
                    contentStyle={CHART_TOOLTIP_STYLE}
                    formatter={(value: number) => [`${value}`, 'Score']}
                  />
                  <Line
                    type="monotone"
                    dataKey="score"
                    stroke="#8b5cf6"
                    strokeWidth={2.5}
                    dot={{ r: 3, fill: '#8b5cf6', strokeWidth: 2, stroke: '#fff' }}
                    activeDot={{ r: 5, fill: '#8b5cf6', strokeWidth: 2, stroke: '#fff' }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="mt-4 flex items-center justify-center rounded-xl border border-dashed border-gray-200 bg-gray-50/50 py-6">
              <p className="text-xs text-gray-400">Complete more interviews to see your trend</p>
            </div>
          )}

          {/* Confidence + communication trends */}
          {stats.trend.length > 1 && (
            <div className="mt-3">
              <p className="mb-1.5 text-[11px] font-semibold uppercase tracking-wide text-gray-400">
                Confidence &amp; Communication
              </p>
              <ResponsiveContainer width="100%" height={80}>
                <LineChart
                  data={stats.trend}
                  margin={{ top: 6, right: 4, left: -22, bottom: 0 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 10, fill: '#9ca3af' }}
                    axisLine={{ stroke: '#e5e7eb' }}
                    tickLine={false}
                    interval="preserveStartEnd"
                  />
                  <YAxis
                    domain={[0, 100]}
                    tick={{ fontSize: 10, fill: '#9ca3af' }}
                    axisLine={false}
                    tickLine={false}
                  />
                  <Tooltip
                    contentStyle={CHART_TOOLTIP_STYLE}
                    formatter={(value: number, name: string) => [`${value}`, name]}
                  />
                  <Line
                    type="monotone"
                    dataKey="confidence"
                    stroke="#3b82f6"
                    strokeWidth={2}
                    dot={false}
                    name="Confidence"
                  />
                  <Line
                    type="monotone"
                    dataKey="communication"
                    stroke="#10b981"
                    strokeWidth={2}
                    dot={false}
                    name="Communication"
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          )}

          {/* Skills + pace */}
          <div className="mt-4 grid grid-cols-3 gap-2.5">
            <StatTile
              label="Confidence"
              value={stats.latestConfidence ?? '—'}
              delta={stats.confidenceDelta}
            />
            <StatTile
              label="Communication"
              value={stats.latestCommunication ?? '—'}
              delta={stats.communicationDelta}
            />
            <StatTile
              label="Pace (wpm)"
              value={stats.latestPace != null ? Math.round(stats.latestPace) : '—'}
              delta={stats.paceDelta}
              subtext={
                stats.avgPace != null ? `${Math.round(stats.avgPace)} wpm avg` : undefined
              }
            />
          </div>

          {/* Completion trend */}
          {stats.completionTrend.length > 1 && (
            <div className="mt-4">
              <p className="mb-1.5 text-[11px] font-semibold uppercase tracking-wide text-gray-400">
                Interview Completion Trend
              </p>
              <ResponsiveContainer width="100%" height={60}>
                <LineChart
                  data={stats.completionTrend}
                  margin={{ top: 6, right: 4, left: -22, bottom: 0 }}
                >
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
                    contentStyle={CHART_TOOLTIP_STYLE}
                    formatter={(value: number) => [`${value}`, 'Completed']}
                  />
                  <Line
                    type="monotone"
                    dataKey="completed"
                    stroke="#f59e0b"
                    strokeWidth={2}
                    dot={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          )}

          {/* Success rate */}
          {stats.successRate != null && (
            <div className="mt-4">
              <div className="flex items-center justify-between text-[11px] text-gray-400">
                <span className="inline-flex items-center gap-1">
                  <CheckCircle2 className="h-3 w-3 text-emerald-500" />
                  Sessions scoring 70+
                </span>
                <span className="font-semibold text-gray-700">{stats.successRate}%</span>
              </div>
              <div className="mt-1 h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
                <div
                  className="h-full rounded-full bg-gradient-to-r from-violet-500 to-emerald-500 transition-all duration-1000"
                  style={{ width: `${Math.min(100, Math.max(0, stats.successRate))}%` }}
                />
              </div>
            </div>
          )}

          {data.mockInterviewInsight && (
            <p className="mt-3 flex items-start gap-2 rounded-xl bg-violet-50 px-3 py-2.5 text-xs leading-relaxed text-violet-700">
              <Gauge className="mt-0.5 h-3.5 w-3.5 shrink-0" />
              {data.mockInterviewInsight}
            </p>
          )}
        </div>
      )}
    </AnalyticsCard>
  );
};
