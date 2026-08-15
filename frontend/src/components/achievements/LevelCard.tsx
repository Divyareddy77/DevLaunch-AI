/**
 * LevelCard — the hero gamification card.
 *
 * Shows the current level in a floating ring, the level title, the total
 * XP, the animated XP bar within the current level, and the XP still
 * needed to reach the next level.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Trophy, Zap, TrendingUp } from 'lucide-react';
import type { AchievementSummary } from '../../types/achievement';
import { ProgressRing } from './ProgressRing';

interface LevelCardProps {
  summary: AchievementSummary;
}

export const LevelCard: React.FC<LevelCardProps> = ({ summary }) => {
  return (
    <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-indigo-600 via-indigo-500 to-violet-600 p-6 text-white shadow-lg">
      {/* Decorative blobs */}
      <div className="pointer-events-none absolute -right-10 -top-10 h-48 w-48 rounded-full bg-white/10 blur-2xl" />
      <div className="pointer-events-none absolute -bottom-16 -left-8 h-44 w-44 rounded-full bg-fuchsia-400/20 blur-2xl" />

      <div className="relative flex items-center gap-6">
        <ProgressRing
          percent={summary.levelProgressPercent}
          size={130}
          strokeWidth={10}
          color="#fbbf24"
          colorTo="#a78bfa"
        >
          <div className="text-center">
            <div className="text-4xl font-extrabold leading-none">{summary.level}</div>
            <div className="mt-1 text-[10px] font-semibold uppercase tracking-widest text-indigo-100">
              Level
            </div>
          </div>
        </ProgressRing>

        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <Trophy className="h-5 w-5 text-amber-300" />
            <h2 className="text-xl font-bold">{summary.levelTitle}</h2>
          </div>

          <p className="mt-1 text-sm text-indigo-100">
            {summary.xpIntoLevel} XP earned in this level
          </p>

          {/* XP progress bar */}
          <div className="mt-4">
            <div className="flex items-center justify-between text-xs text-indigo-100">
              <span className="font-semibold">
                {summary.currentLevelXp} XP
              </span>
              <span>{summary.nextLevelXp} XP</span>
            </div>
            <div className="mt-1.5 h-2.5 overflow-hidden rounded-full bg-white/20">
              <div
                className="h-full rounded-full bg-gradient-to-r from-amber-300 to-amber-400 xp-bar-fill"
                style={{ width: `${summary.levelProgressPercent}%` }}
              />
            </div>
          </div>

          <div className="mt-4 flex items-center gap-4 text-sm">
            <span className="inline-flex items-center gap-1.5">
              <Zap className="h-4 w-4 text-amber-300" />
              <span className="font-bold">{summary.totalXp}</span>
              <span className="text-indigo-100">total XP</span>
            </span>
            <span className="inline-flex items-center gap-1.5">
              <TrendingUp className="h-4 w-4 text-emerald-300" />
              <span className="text-indigo-100">
                {summary.xpNeededForNext} XP to Level {summary.nextLevel}
              </span>
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
