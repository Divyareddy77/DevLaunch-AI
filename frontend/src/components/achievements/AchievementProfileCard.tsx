/**
 * AchievementProfileCard — the gamification summary on the profile page.
 *
 * Shows the level badge, total XP, unlocked badge count, and the latest
 * badge, fetched from the achievements summary endpoint. Links to the
 * achievements page.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Trophy, Zap, Medal, ChevronRight } from 'lucide-react';
import { achievementService } from '../../services/achievement.service';
import type { AchievementSummary } from '../../types/achievement';
import { ROUTES } from '../../constants/routes';
import { ProgressRing } from './ProgressRing';

export const AchievementProfileCard: React.FC = () => {
  const navigate = useNavigate();
  const [summary, setSummary] = useState<AchievementSummary | null>(null);

  useEffect(() => {
    achievementService
      .getSummary()
      .then(setSummary)
      .catch(() => setSummary(null));
  }, []);

  return (
    <button
      onClick={() => navigate(ROUTES.ACHIEVEMENTS)}
      className="group w-full rounded-2xl border border-gray-200 bg-white p-5 text-left shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md"
    >
      <div className="flex items-center justify-between">
        <h3 className="flex items-center gap-2 text-sm font-semibold text-gray-800">
          <Trophy className="h-4 w-4 text-amber-500" />
          Gamification
        </h3>
        <ChevronRight className="h-4 w-4 text-gray-300 transition-transform group-hover:translate-x-0.5" />
      </div>

      {!summary ? (
        <p className="mt-4 text-xs text-gray-400">
          Loading your achievements…
        </p>
      ) : (
        <div className="mt-4 flex items-center gap-5">
          {/* Level badge */}
          <ProgressRing
            percent={summary.levelProgressPercent}
            size={76}
            strokeWidth={7}
            color="#f59e0b"
            colorTo="#8b5cf6"
          >
            <div className="text-center">
              <div className="text-xl font-extrabold text-gray-900">{summary.level}</div>
              <div className="text-[9px] font-semibold uppercase tracking-widest text-gray-400">
                Level
              </div>
            </div>
          </ProgressRing>

          <div className="min-w-0 flex-1 space-y-2.5">
            <div className="flex items-center gap-2 text-sm">
              <Zap className="h-4 w-4 text-amber-500" />
              <span className="font-bold text-gray-900">{summary.totalXp.toLocaleString()}</span>
              <span className="text-xs text-gray-400">total XP</span>
            </div>

            <div className="flex items-center gap-2 text-sm">
              <Medal className="h-4 w-4 text-indigo-500" />
              <span className="font-bold text-gray-900">{summary.unlockedCount}</span>
              <span className="text-xs text-gray-400">
                of {summary.totalAchievements} badges
              </span>
            </div>

            {summary.latestUnlock && (
              <div className="flex items-center gap-2">
                <span
                  className="flex h-6 w-6 items-center justify-center rounded-md text-sm"
                  style={{ backgroundColor: `${summary.latestUnlock.color}1a` }}
                >
                  {summary.latestUnlock.icon}
                </span>
                <span className="truncate text-xs text-gray-500">
                  Latest: {summary.latestUnlock.title}
                </span>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Level progress */}
      {summary && (
        <div className="mt-4">
          <div className="flex items-center justify-between text-[11px] text-gray-400">
            <span>{summary.levelTitle}</span>
            <span>
              {summary.xpNeededForNext} XP to Level {summary.nextLevel}
            </span>
          </div>
          <div className="mt-1 h-1.5 overflow-hidden rounded-full bg-gray-100">
            <div
              className="h-full rounded-full bg-gradient-to-r from-amber-400 to-violet-500 transition-all duration-1000"
              style={{ width: `${summary.levelProgressPercent}%` }}
            />
          </div>
        </div>
      )}
    </button>
  );
};
