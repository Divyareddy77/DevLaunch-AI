/**
 * InterviewAnalyticsCard — the interview analytics view.
 *
 * Shows the aggregate statistics (average, best, most practised category,
 * total time spent, questions answered, success rate) and the score trend
 * chart over the most recent interviews.
 *
 * @author DevLaunch
 */

import React from 'react';
import { BarChart3, Award, Flame, Clock, MessageSquare, TrendingUp, Target } from 'lucide-react';
import { format, parseISO } from 'date-fns';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Spinner } from '../ui/Spinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { LineChart, type LineChartDataPoint } from '../charts/LineChart';
import { enumToLabel, formatDuration, formatNumber } from '../../utils/format';
import type { InterviewHistoryResponse } from '../../types/ai';

interface InterviewAnalyticsCardProps {
  /** The aggregate history data, or null while loading. */
  data: InterviewHistoryResponse | null;
  /** Whether the analytics are loading. */
  loading: boolean;
  /** A load error message, if any. */
  error: string | null;
  /** Called to retry loading. */
  onRetry: () => void;
}

/** A single analytics statistic tile. */
const StatTile: React.FC<{
  icon: React.ComponentType<{ className?: string }>;
  iconClassName: string;
  label: string;
  value: string;
  sub?: string;
}> = ({ icon: Icon, iconClassName, label, value, sub }) => (
  <div className="rounded-xl border border-gray-100 p-4">
    <div className="flex items-center gap-2.5">
      <div className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-lg ${iconClassName}`}>
        <Icon className="h-4 w-4" />
      </div>
      <p className="text-xs font-medium text-gray-500">{label}</p>
    </div>
    <p className="mt-2 text-2xl font-bold text-gray-900">{value}</p>
    {sub && <p className="mt-0.5 text-xs text-gray-400">{sub}</p>}
  </div>
);

export const InterviewAnalyticsCard: React.FC<InterviewAnalyticsCardProps> = ({
  data,
  loading,
  error,
  onRetry,
}) => {
  return (
    <Card
      header={
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-violet-100">
            <BarChart3 className="h-5 w-5 text-violet-600" />
          </div>
          <h3 className="text-sm font-semibold text-gray-900">Interview Analytics</h3>
        </div>
      }
    >
      {loading && !data ? (
        <div className="flex justify-center py-8">
          <Spinner label="Loading analytics…" />
        </div>
      ) : error && !data ? (
        <ErrorMessage message={error} onRetry={onRetry} />
      ) : data && data.totalInterviews === 0 ? (
        <p className="py-6 text-center text-sm text-gray-400">
          Complete your first interview to unlock analytics.
        </p>
      ) : data ? (
        <div className="space-y-5">
          <div className="grid grid-cols-2 gap-3 lg:grid-cols-3">
            <StatTile
              icon={TrendingUp}
              iconClassName="bg-indigo-100 text-indigo-600"
              label="Average score"
              value={data.averageScore.toFixed(1)}
              sub="/ 100"
            />
            <StatTile
              icon={Award}
              iconClassName="bg-emerald-100 text-emerald-600"
              label="Best score"
              value={data.bestScore != null ? String(data.bestScore) : '—'}
              sub="/ 100"
            />
            <StatTile
              icon={Target}
              iconClassName="bg-amber-100 text-amber-600"
              label="Most practised"
              value={data.mostPracticedCategory ? enumToLabel(data.mostPracticedCategory) : '—'}
            />
            <StatTile
              icon={Clock}
              iconClassName="bg-sky-100 text-sky-600"
              label="Time spent"
              value={formatDuration(data.totalTimeSpentSeconds)}
            />
            <StatTile
              icon={MessageSquare}
              iconClassName="bg-rose-100 text-rose-600"
              label="Questions answered"
              value={formatNumber(data.totalQuestionsAnswered)}
            />
            <StatTile
              icon={Flame}
              iconClassName="bg-orange-100 text-orange-600"
              label="Success rate"
              value={data.successRate != null ? `${data.successRate}%` : '—'}
              sub="scores ≥ 70"
            />
          </div>

          {data.scoreTrend && data.scoreTrend.length > 1 && (
            <div>
              <div className="mb-2 flex items-center gap-2">
                <Badge variant="default" size="sm">
                  Last {data.scoreTrend.length} interviews
                </Badge>
                <span className="text-xs text-gray-400">
                  Score trend — newest first
                </span>
              </div>
              <LineChart
                data={data.scoreTrend
                  .slice()
                  .reverse()
                  .map<LineChartDataPoint>((point) => ({
                    name: format(parseISO(point.completedAt), 'MMM d'),
                    value: point.score,
                  }))}
                lineColor="#7c3aed"
                lineLabel="Score"
                yAxisLabel="Score"
              />
            </div>
          )}
        </div>
      ) : null}
    </Card>
  );
};
