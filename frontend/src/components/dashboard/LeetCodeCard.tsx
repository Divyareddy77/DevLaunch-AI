/**
 * LeetCodeCard — dashboard widget summarising the user's LeetCode progress.
 *
 * Displays total problems solved broken down by difficulty level,
 * and a link to view the full LeetCode tracker page.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Code2, ExternalLink } from 'lucide-react';
import { DashboardCard } from './DashboardCard';

interface LeetCodeCardProps {
  /** Total number of problems solved on LeetCode. */
  totalSolved: number | null;
  /** Number of easy-difficulty problems solved. */
  easySolved: number | null;
  /** Number of medium-difficulty problems solved. */
  mediumSolved: number | null;
  /** Number of hard-difficulty problems solved. */
  hardSolved: number | null;
  /** Whether the card data is still loading. */
  loading?: boolean;
  /** Error message to display if data could not be loaded. */
  error?: string | null;
  /** Callback to navigate to the full LeetCode tracker page. */
  onViewAll?: () => void;
}

export const LeetCodeCard: React.FC<LeetCodeCardProps> = ({
  totalSolved,
  easySolved,
  mediumSolved,
  hardSolved,
  loading = false,
  error = null,
  onViewAll,
}) => {
  return (
    <DashboardCard
      title="LeetCode Progress"
      icon={<Code2 className="h-5 w-5" />}
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
          <div className="mb-3 flex items-baseline justify-center gap-2">
            <span className="text-3xl font-bold text-gray-900">{totalSolved ?? '—'}</span>
            <span className="text-sm text-gray-500">problems solved</span>
          </div>

          <div className="space-y-2">
            {/* Easy */}
            <div className="flex items-center gap-2">
              <div className="h-2.5 w-full rounded-full bg-gray-100">
                <div
                  className="h-2.5 rounded-full bg-emerald-500 transition-all"
                  style={{
                    width: totalSolved && easySolved ? `${(easySolved / totalSolved) * 100}%` : '0%',
                  }}
                />
              </div>
              <span className="w-16 text-right text-xs font-medium text-emerald-600">
                {easySolved ?? 0} Easy
              </span>
            </div>

            {/* Medium */}
            <div className="flex items-center gap-2">
              <div className="h-2.5 w-full rounded-full bg-gray-100">
                <div
                  className="h-2.5 rounded-full bg-amber-500 transition-all"
                  style={{
                    width: totalSolved && mediumSolved ? `${(mediumSolved / totalSolved) * 100}%` : '0%',
                  }}
                />
              </div>
              <span className="w-16 text-right text-xs font-medium text-amber-600">
                {mediumSolved ?? 0} Med
              </span>
            </div>

            {/* Hard */}
            <div className="flex items-center gap-2">
              <div className="h-2.5 w-full rounded-full bg-gray-100">
                <div
                  className="h-2.5 rounded-full bg-red-500 transition-all"
                  style={{
                    width: totalSolved && hardSolved ? `${(hardSolved / totalSolved) * 100}%` : '0%',
                  }}
                />
              </div>
              <span className="w-16 text-right text-xs font-medium text-red-600">
                {hardSolved ?? 0} Hard
              </span>
            </div>
          </div>
        </div>
      )}
    </DashboardCard>
  );
};
