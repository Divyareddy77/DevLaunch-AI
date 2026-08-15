/**
 * RecentUnlockTimeline — a vertical timeline of the most recent badge
 * unlocks.
 *
 * Each entry shows the badge icon tile, title, category, XP reward, and
 * the unlock date. Used on the achievements page and the dashboard widget.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Clock } from 'lucide-react';
import {
  ACHIEVEMENT_CATEGORY_LABELS,
  ACHIEVEMENT_CATEGORY_STYLES,
  type UnlockedAchievement,
} from '../../types/achievement';
import { formatDate } from '../../utils/date';

interface RecentUnlockTimelineProps {
  unlocks: UnlockedAchievement[];
  /** Whether to show the section header. */
  showHeader?: boolean;
}

export const RecentUnlockTimeline: React.FC<RecentUnlockTimelineProps> = ({
  unlocks,
  showHeader = true,
}) => {
  if (unlocks.length === 0) {
    return (
      <div className="rounded-2xl border border-dashed border-gray-200 bg-white p-6 text-center">
        <Clock className="mx-auto h-8 w-8 text-gray-300" />
        <p className="mt-2 text-sm font-medium text-gray-500">No unlocks yet</p>
        <p className="mt-0.5 text-xs text-gray-400">
          Complete tasks across the platform to earn your first badge
        </p>
      </div>
    );
  }

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
      {showHeader && (
        <h3 className="text-sm font-semibold text-gray-800">Recent unlocks</h3>
      )}

      <ol className={`${showHeader ? 'mt-4' : ''} space-y-4`}>
        {unlocks.map((unlock, index) => (
          <li key={unlock.id} className="flex items-center gap-3 animate-fade-in-up" style={{ animationDelay: `${index * 60}ms` }}>
            <div
              className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-xl text-lg"
              style={{ backgroundColor: `${unlock.color}1a` }}
            >
              <span aria-hidden="true">{unlock.icon}</span>
            </div>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-semibold text-gray-800">{unlock.title}</p>
              <p className="mt-0.5 flex items-center gap-1.5 text-[11px] text-gray-400">
                <span
                  className={`rounded-full px-1.5 py-px font-medium ${ACHIEVEMENT_CATEGORY_STYLES[unlock.category]}`}
                >
                  {ACHIEVEMENT_CATEGORY_LABELS[unlock.category]}
                </span>
                {unlock.unlockedAt ? formatDate(unlock.unlockedAt) : ''}
              </p>
            </div>
            <span
              className="flex-shrink-0 rounded-lg px-2 py-1 text-xs font-bold"
              style={{ backgroundColor: `${unlock.color}1a`, color: unlock.color }}
            >
              +{unlock.xpReward} XP
            </span>
          </li>
        ))}
      </ol>
    </div>
  );
};
