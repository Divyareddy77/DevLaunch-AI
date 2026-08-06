/**
 * LeetCodeCard — dashboard widget summarising the user's LeetCode progress.
 *
 * When a LeetCode account is linked it displays the total problems solved
 * broken down by difficulty, the acceptance rate, and the global ranking.
 * When no account is connected it shows a friendly prompt with a Connect
 * button that navigates to the LeetCode Tracker page.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Code2, ExternalLink, Link2, Trophy } from 'lucide-react';
import { DashboardCard } from './DashboardCard';

interface LeetCodeCardProps {
  /** Whether a LeetCode account is linked to the user. */
  connected: boolean;
  /** The linked LeetCode username, or null when not connected. */
  username: string | null;
  /** Total number of problems solved on LeetCode. */
  totalSolved: number | null;
  /** Number of easy-difficulty problems solved. */
  easySolved: number | null;
  /** Number of medium-difficulty problems solved. */
  mediumSolved: number | null;
  /** Number of hard-difficulty problems solved. */
  hardSolved: number | null;
  /** The user's LeetCode acceptance rate as a percentage, or null. */
  acceptanceRate: number | null;
  /** The user's global LeetCode ranking, or null. */
  ranking: number | null;
  /** Whether the card data is still loading. */
  loading?: boolean;
  /** Error message to display if data could not be loaded. */
  error?: string | null;
  /** Callback to navigate to the full LeetCode tracker page. */
  onViewAll?: () => void;
  /** Callback to navigate to the LeetCode tracker page to connect an account. */
  onConnect?: () => void;
}

export const LeetCodeCard: React.FC<LeetCodeCardProps> = ({
  connected,
  username,
  totalSolved,
  easySolved,
  mediumSolved,
  hardSolved,
  acceptanceRate,
  ranking,
  loading = false,
  error = null,
  onViewAll,
  onConnect,
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
            View Tracker
            <ExternalLink className="h-3 w-3" />
          </button>
        )
      }
      loading={loading}
    >
      {error ? (
        <p className="text-sm text-red-500">{error}</p>
      ) : !connected ? (
        /* ---- Not connected: prompt to link an account ---- */
        <div className="flex flex-col items-center py-4 text-center">
          <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-gray-100">
            <Code2 className="h-6 w-6 text-gray-400" />
          </div>
          <p className="text-sm font-medium text-gray-700">No LeetCode account connected</p>
          <p className="mt-1 max-w-[220px] text-xs leading-relaxed text-gray-400">
            Connect your LeetCode account to display your progress.
          </p>
          {onConnect && (
            <button
              onClick={onConnect}
              className="mt-4 inline-flex items-center gap-1.5 rounded-lg bg-primary-600 px-3.5 py-2 text-xs font-medium text-white shadow-sm transition-colors hover:bg-primary-700"
            >
              <Link2 className="h-3.5 w-3.5" />
              Connect
            </button>
          )}
        </div>
      ) : (
        /* ---- Connected: live statistics ---- */
        <div>
          <p className="mb-3 truncate text-center text-xs font-medium text-gray-500">
            @{username}
          </p>

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
                    width: totalSolved && mediumSolved
                      ? `${(mediumSolved / totalSolved) * 100}%`
                      : '0%',
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

          {/* Acceptance rate + ranking */}
          <div className="mt-4 grid grid-cols-2 gap-3 border-t border-gray-100 pt-3">
            <div className="text-center">
              <p className="text-sm font-bold text-gray-900">
                {acceptanceRate != null ? `${acceptanceRate.toFixed(1)}%` : '—'}
              </p>
              <p className="mt-0.5 text-xs text-gray-500">Acceptance Rate</p>
            </div>
            <div className="text-center">
              <p className="flex items-center justify-center gap-1 text-sm font-bold text-gray-900">
                <Trophy className="h-3.5 w-3.5 text-gray-400" />
                {ranking != null ? `#${ranking.toLocaleString()}` : '—'}
              </p>
              <p className="mt-0.5 text-xs text-gray-500">Ranking</p>
            </div>
          </div>
        </div>
      )}
    </DashboardCard>
  );
};
