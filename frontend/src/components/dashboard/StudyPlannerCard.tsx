/**
 * StudyPlannerCard — dashboard widget showing the user's study
 * session progress.
 *
 * Displays completed vs. total tasks and a link to the full
 * study planner page.
 *
 * @author DevLaunch
 */

import React from 'react';
import { CalendarCheck, ExternalLink, TrendingUp } from 'lucide-react';
import { DashboardCard } from './DashboardCard';

interface StudyPlannerCardProps {
  /** Total number of study tasks created. */
  totalTasks: number | null;
  /** Number of study tasks marked as completed. */
  completedTasks: number | null;
  /** Whether the card data is still loading. */
  loading?: boolean;
  /** Error message to display if data could not be loaded. */
  error?: string | null;
  /** Callback to navigate to the full study planner page. */
  onViewAll?: () => void;
}

export const StudyPlannerCard: React.FC<StudyPlannerCardProps> = ({
  totalTasks,
  completedTasks,
  loading = false,
  error = null,
  onViewAll,
}) => {
  const completionRate =
    totalTasks && completedTasks !== null && totalTasks > 0
      ? Math.round((completedTasks / totalTasks) * 100)
      : 0;

  return (
    <DashboardCard
      title="Study Progress"
      icon={<CalendarCheck className="h-5 w-5" />}
      action={
        onViewAll && (
          <button
            onClick={onViewAll}
            className="flex items-center gap-1 text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
          >
            View All
            <ExternalLink className="h-3 w-3" />
          </button>
        )
      }
      loading={loading}
    >
      {error ? (
        <p className="text-sm text-red-500">{error}</p>
      ) : (
        <div>
          <div className="mb-3 flex items-center justify-center gap-3">
            <div className="text-center">
              <p className="text-2xl font-bold text-gray-900">{completedTasks ?? 0}</p>
              <p className="text-xs text-gray-500">Completed</p>
            </div>
            <span className="text-gray-300 text-xl">/</span>
            <div className="text-center">
              <p className="text-2xl font-bold text-gray-900">{totalTasks ?? 0}</p>
              <p className="text-xs text-gray-500">Total</p>
            </div>
          </div>

          {/* Progress bar */}
          <div className="relative h-2 w-full overflow-hidden rounded-full bg-gray-100">
            <div
              className="h-full rounded-full bg-indigo-500 transition-all duration-500"
              style={{ width: `${completionRate}%` }}
            />
          </div>

          <div className="mt-2 flex items-center justify-center gap-1">
            <TrendingUp className={`h-4 w-4 ${completionRate >= 50 ? 'text-emerald-500' : 'text-amber-500'}`} />
            <span className={`text-xs font-medium ${completionRate >= 50 ? 'text-emerald-600' : 'text-amber-600'}`}>
              {completionRate}% completion rate
            </span>
          </div>
        </div>
      )}
    </DashboardCard>
  );
};
