/**
 * AchievementSummaryCard — the gamification widget on the dashboard.
 *
 * Fetches the achievement summary and shows the current level, total XP,
 * the level progress bar, the latest badge, the unlocked count, and the
 * completion percentage. Clicking navigates to the achievements page.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Trophy, Zap, ChevronRight } from 'lucide-react';
import { achievementService } from '../../services/achievement.service';
import type { AchievementSummary } from '../../types/achievement';
import { ROUTES } from '../../constants/routes';
import { formatDate } from '../../utils/date';

export const AchievementSummaryCard: React.FC = () => {
  const navigate = useNavigate();
  const [summary, setSummary] = useState<AchievementSummary | null>(null);

  useEffect(() => {
    // The summary is a supplementary widget — failures never block the
    // dashboard, so it falls back to the placeholder quietly.
    achievementService
      .getSummary()
      .then(setSummary)
      .catch(() => setSummary(null));
  }, []);

  if (!summary) {
    return (
      <div className="flex flex-col items-center rounded-xl border border-gray-200 bg-white px-5 py-6 shadow-sm">
        <Trophy className="mb-2 h-8 w-8 text-gray-300" />
        <p className="text-sm font-medium text-gray-500">Start your journey</p>
        <p className="mt-0.5 text-center text-xs text-gray-400">
          Complete tasks across modules to earn achievements
        </p>
      </div>
    );
  }

  return (
    <button
      onClick={() => navigate(ROUTES.ACHIEVEMENTS)}
      className="group relative w-full overflow-hidden rounded-xl border border-gray-200 bg-white p-5 text-left shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md"
    >
      {/* Gradient header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-indigo-500 to-violet-600 text-white shadow">
            <Trophy className="h-4 w-4" />
          </span>
          <div>
            <p className="text-sm font-semibold text-gray-800">Level {summary.level}</p>
            <p className="text-[11px] text-gray-400">{summary.levelTitle}</p>
          </div>
        </div>
        <ChevronRight className="h-4 w-4 text-gray-300 transition-transform group-hover:translate-x-0.5" />
      </div>

      {/* XP */}
      <div className="mt-4 flex items-baseline gap-1">
        <Zap className="h-4 w-4 self-center text-amber-500" />
        <span className="text-2xl font-bold text-gray-900">
          {summary.totalXp.toLocaleString()}
        </span>
        <span className="text-xs text-gray-400">XP</span>
      </div>

      {/* Level progress */}
      <div className="mt-3">
        <div className="flex items-center justify-between text-[11px] text-gray-400">
          <span>
            {summary.xpIntoLevel} / {summary.nextLevelXp - summary.currentLevelXp} XP
          </span>
          <span>Level {summary.nextLevel}</span>
        </div>
        <div className="mt-1 h-2 overflow-hidden rounded-full bg-gray-100">
          <div
            className="h-full rounded-full bg-gradient-to-r from-indigo-500 to-violet-500 transition-all duration-1000"
            style={{ width: `${summary.levelProgressPercent}%` }}
          />
        </div>
      </div>

      {/* Latest badge + completion */}
      <div className="mt-4 flex items-center justify-between">
        {summary.latestUnlock ? (
          <div className="flex items-center gap-2">
            <span
              className="flex h-8 w-8 items-center justify-center rounded-lg text-base"
              style={{ backgroundColor: `${summary.latestUnlock.color}1a` }}
            >
              {summary.latestUnlock.icon}
            </span>
            <div>
              <p className="text-xs font-semibold text-gray-700">
                {summary.latestUnlock.title}
              </p>
              <p className="text-[10px] text-gray-400">
                {summary.latestUnlock.unlockedAt
                  ? `Unlocked ${formatDate(summary.latestUnlock.unlockedAt)}`
                  : 'Latest badge'}
              </p>
            </div>
          </div>
        ) : (
          <p className="text-xs text-gray-400">No badges yet — keep exploring!</p>
        )}

        <div className="text-right">
          <p className="text-sm font-bold text-gray-800">
            {summary.unlockedCount}
            <span className="text-xs font-medium text-gray-400">
              /{summary.totalAchievements}
            </span>
          </p>
          <p className="text-[10px] text-gray-400">{summary.completionPercent}% complete</p>
        </div>
      </div>
    </button>
  );
};
