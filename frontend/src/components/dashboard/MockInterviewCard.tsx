/**
 * MockInterviewCard — the dashboard widget summarising the user's mock
 * interview progress.
 *
 * Shows the latest score, the average, the interview count, the best
 * score, the score trend between the two most recent interviews, and a
 * data-driven recommendation.
 *
 * @see backend/src/main/java/com/devlaunch/service/impl/DashboardServiceImpl.java
 * @author DevLaunch
 */

import React from 'react';
import { Mic, TrendingUp, TrendingDown, ArrowRight } from 'lucide-react';
import { DashboardCard } from './DashboardCard';
import { Badge } from '../ui/Badge';
import { getScoreBadgeVariant } from '../../utils/format';

interface MockInterviewCardProps {
  /** The number of completed mock interviews. */
  count: number;
  /** The latest interview score, or null. */
  latestScore: number | null;
  /** The average interview score, or null. */
  averageScore: number | null;
  /** The best interview score, or null. */
  bestScore: number | null;
  /** The score trend (latest minus previous), or null. */
  trend: number | null;
  /** A short recommendation derived from the history. */
  insight: string | null;
  /** Called when the user opens the mock interview module. */
  onViewAll: () => void;
}

export const MockInterviewCard: React.FC<MockInterviewCardProps> = ({
  count,
  latestScore,
  averageScore,
  bestScore,
  trend,
  insight,
  onViewAll,
}) => {
  const hasInterviews = count > 0;

  return (
    <DashboardCard
      title="Mock Interviews"
      icon={<Mic className="h-5 w-5" />}
      action={
        <button
          onClick={onViewAll}
          className="inline-flex items-center gap-1 text-xs font-medium text-indigo-600 transition-colors hover:text-indigo-800"
        >
          Practice
          <ArrowRight className="h-3 w-3" />
        </button>
      }
    >
      {!hasInterviews ? (
        <div className="flex flex-col items-center py-4">
          <Mic className="mb-2 h-8 w-8 text-gray-300" />
          <p className="text-sm font-medium text-gray-500">No interviews yet</p>
          <p className="mt-0.5 text-center text-xs text-gray-400">
            Complete a mock interview to see your progress
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          <div className="grid grid-cols-3 gap-2 text-center">
            <div className="rounded-lg bg-gray-50 p-2">
              <p className="text-lg font-bold text-gray-900">
                {latestScore != null ? latestScore : '—'}
              </p>
              <p className="text-[10px] text-gray-500">Latest</p>
            </div>
            <div className="rounded-lg bg-gray-50 p-2">
              <p className="text-lg font-bold text-gray-900">
                {averageScore != null ? averageScore.toFixed(0) : '—'}
              </p>
              <p className="text-[10px] text-gray-500">Average</p>
            </div>
            <div className="rounded-lg bg-gray-50 p-2">
              <p className="text-lg font-bold text-gray-900">{bestScore ?? '—'}</p>
              <p className="text-[10px] text-gray-500">Best</p>
            </div>
          </div>

          <div className="flex items-center justify-between">
            <Badge variant="default" size="sm">
              {count} {count === 1 ? 'interview' : 'interviews'}
            </Badge>
            {trend != null &&
              (trend >= 0 ? (
                <span className="inline-flex items-center gap-1 text-xs font-medium text-emerald-600">
                  <TrendingUp className="h-3.5 w-3.5" />
                  +{trend} vs last
                </span>
              ) : (
                <span className="inline-flex items-center gap-1 text-xs font-medium text-red-500">
                  <TrendingDown className="h-3.5 w-3.5" />
                  {trend} vs last
                </span>
              ))}
          </div>

          {latestScore != null && (
            <div className="flex items-center justify-between gap-3">
              <span className="text-xs text-gray-500">Latest performance</span>
              <Badge variant={getScoreBadgeVariant(latestScore)} size="sm">
                {latestScore}/100
              </Badge>
            </div>
          )}

          {insight && (
            <p className="rounded-lg bg-indigo-50 px-3 py-2 text-xs leading-relaxed text-indigo-700">
              {insight}
            </p>
          )}
        </div>
      )}
    </DashboardCard>
  );
};
