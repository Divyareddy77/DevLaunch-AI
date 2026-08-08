/**
 * AchievementProfileCard — the gamification summary on the profile page.
 *
 * Shows the level badge, total XP, unlocked badge count, the latest badge,
 * an animated level-progress bar with the XP percentage, and a preview of
 * the next badge to unlock, all fetched from the achievements endpoints.
 * Links to the achievements page.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Trophy, Medal, TrendingUp, ChevronRight } from 'lucide-react';
import { achievementService } from '../../services/achievement.service';
import type {
  AchievementProgress,
  AchievementSummary,
} from '../../types/achievement';
import { ROUTES } from '../../constants/routes';
import { ProgressRing } from './ProgressRing';
import { CountUp } from '../ui/CountUp';

export const AchievementProfileCard: React.FC = () => {
  const navigate = useNavigate();
  const [summary, setSummary] = useState<AchievementSummary | null>(null);
  const [nextBadge, setNextBadge] = useState<AchievementProgress | null>(null);

  useEffect(() => {
    achievementService
      .getSummary()
      .then(setSummary)
      .catch(() => setSummary(null));

    // The closest locked badge (highest progress) becomes the next-badge preview.
    achievementService
      .getProgress()
      .then((progress) => {
        const next = progress
          .filter((item) => !item.unlocked)
          .reduce<AchievementProgress | null>(
            (best, item) =>
              best === null || item.progressPercent > best.progressPercent ? item : best,
            null,
          );
        setNextBadge(next);
      })
      .catch(() => setNextBadge(null));
  }, []);

  return (
    <button
      onClick={() => navigate(ROUTES.ACHIEVEMENTS)}
      className="group w-full overflow-hidden rounded-2xl border border-gray-100 bg-white text-left shadow-[0_1px_3px_rgba(16,24,40,0.06)] transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_16px_40px_-12px_rgba(16,24,40,0.18)]"
    >
      {/* Header */}
      <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
        <h3 className="flex items-center gap-2 text-sm font-semibold text-gray-900">
          <Trophy className="h-4 w-4 text-amber-500" />
          Gamification
        </h3>
        <ChevronRight className="h-4 w-4 text-gray-300 transition-transform duration-300 group-hover:translate-x-0.5 group-hover:text-gray-400" />
      </div>

      <div className="p-6">
        {!summary ? (
          <p className="text-xs text-gray-400">Loading your achievements…</p>
        ) : (
          <>
            {/* Level ring + stats */}
            <div className="flex items-center gap-5">
              <ProgressRing
                percent={summary.levelProgressPercent}
                size={92}
                strokeWidth={8}
                color="#f59e0b"
                colorTo="#8b5cf6"
              >
                <div className="text-center">
                  <div className="text-2xl font-extrabold leading-none text-gray-900">
                    {summary.level}
                  </div>
                  <div className="mt-0.5 text-[9px] font-semibold uppercase tracking-widest text-gray-400">
                    Level
                  </div>
                </div>
              </ProgressRing>

              <div className="min-w-0 flex-1 space-y-2">
                <p className="truncate text-xs font-semibold uppercase tracking-wide text-gray-400">
                  {summary.levelTitle}
                </p>
                <p className="text-sm">
                  <CountUp
                    value={summary.totalXp}
                    suffix=" XP"
                    className="text-lg font-bold text-gray-900"
                  />
                </p>
                <p className="flex items-center gap-1.5 text-xs text-gray-500">
                  <Medal className="h-3.5 w-3.5 text-indigo-500" />
                  <span className="font-semibold text-gray-800">{summary.unlockedCount}</span>
                  of {summary.totalAchievements} badges
                </p>
                {summary.latestUnlock && (
                  <p className="flex items-center gap-1.5 truncate text-xs text-gray-400">
                    <span
                      className="flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-md text-xs"
                      style={{ backgroundColor: `${summary.latestUnlock.color}1a` }}
                    >
                      {summary.latestUnlock.icon}
                    </span>
                    <span className="truncate">Latest: {summary.latestUnlock.title}</span>
                  </p>
                )}
              </div>
            </div>

            {/* Animated level progress */}
            <div className="mt-5">
              <div className="flex items-center justify-between text-xs">
                <span className="font-medium text-gray-500">
                  {summary.currentLevelXp} XP
                </span>
                <span className="font-bold text-gray-900">
                  {summary.levelProgressPercent}%
                </span>
              </div>
              <div className="relative mt-1.5 h-2 overflow-hidden rounded-full bg-gray-100">
                <div
                  className="xp-bar-fill h-full rounded-full bg-gradient-to-r from-amber-400 to-violet-500"
                  style={{ width: `${summary.levelProgressPercent}%` }}
                />
                <div
                  className="xp-bar-shimmer pointer-events-none absolute inset-y-0 w-1/3 bg-gradient-to-r from-transparent via-white/70 to-transparent"
                  aria-hidden="true"
                />
              </div>
              <p className="mt-2 flex items-center gap-1.5 text-[11px] text-gray-400">
                <TrendingUp className="h-3 w-3 text-emerald-500" />
                <span className="font-semibold text-gray-600">
                  {summary.xpNeededForNext} XP
                </span>
                needed for Level {summary.nextLevel}
              </p>
            </div>

            {/* Next badge preview */}
            {nextBadge && (
              <div className="mt-4 rounded-xl border border-dashed border-gray-200 bg-gray-50/70 p-3.5">
                <p className="text-[10px] font-semibold uppercase tracking-wider text-gray-400">
                  Next badge
                </p>
                <div className="mt-2 flex items-center gap-3">
                  <span
                    className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg text-lg"
                    style={{ backgroundColor: `${nextBadge.color}1a` }}
                  >
                    {nextBadge.icon}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-xs font-semibold text-gray-800">
                      {nextBadge.title}
                    </p>
                    <p className="text-[11px] text-gray-400">
                      {nextBadge.progressPercent}% complete
                    </p>
                  </div>
                </div>
                <div className="mt-2 h-1 overflow-hidden rounded-full bg-gray-200">
                  <div
                    className="h-full rounded-full bg-indigo-500 transition-[width] duration-1000 ease-out"
                    style={{ width: `${nextBadge.progressPercent}%` }}
                  />
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </button>
  );
};
