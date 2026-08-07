/**
 * AchievementCard — a badge card in the achievements grid.
 *
 * Shows the badge icon, colour, title, description, XP reward, category,
 * unlock condition, current progress, and locked/unlocked status. Locked
 * cards render muted/grayscale; unlocked cards render in full colour with
 * a soft glow and the unlock date.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Lock, Sparkles, Check } from 'lucide-react';
import {
  ACHIEVEMENT_CATEGORY_LABELS,
  ACHIEVEMENT_CATEGORY_STYLES,
  type AchievementProgress,
} from '../../types/achievement';
import { formatDate } from '../../utils/date';

interface AchievementCardProps {
  achievement: AchievementProgress;
  /** Animation delay (ms) for the staggered entrance. */
  delay?: number;
}

export const AchievementCard: React.FC<AchievementCardProps> = ({
  achievement,
  delay = 0,
}) => {
  const { unlocked, icon, title, description, color, xpReward, category } = achievement;

  return (
    <div
      className={`group relative overflow-hidden rounded-2xl border bg-white p-5 shadow-sm transition-all duration-300 animate-fade-in-up hover:-translate-y-1 hover:shadow-lg ${
        unlocked
          ? 'border-amber-200 hover:border-amber-300'
          : 'border-gray-200 hover:border-gray-300'
      }`}
      style={{ animationDelay: `${delay}ms` }}
    >
      {/* Top colour accent */}
      <div
        className="absolute inset-x-0 top-0 h-1"
        style={{ background: unlocked ? color : '#e5e7eb' }}
      />

      <div className="flex items-start gap-3">
        {/* Badge icon tile */}
        <div
          className={`flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-xl text-2xl transition-transform duration-300 group-hover:scale-110 ${
            unlocked ? '' : 'grayscale opacity-60'
          }`}
          style={{ backgroundColor: `${color}1a` }}
        >
          <span aria-hidden="true">{icon}</span>
        </div>

        <div className="min-w-0 flex-1">
          <div className="flex items-center justify-between gap-2">
            <h3
              className={`truncate text-sm font-semibold ${
                unlocked ? 'text-gray-900' : 'text-gray-500'
              }`}
            >
              {title}
            </h3>
            {unlocked ? (
              <span className="flex flex-shrink-0 items-center gap-1 rounded-full bg-amber-100 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-amber-700">
                <Check className="h-3 w-3" /> Unlocked
              </span>
            ) : (
              <span className="flex flex-shrink-0 items-center gap-1 rounded-full bg-gray-100 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-gray-500">
                <Lock className="h-3 w-3" /> Locked
              </span>
            )}
          </div>

          <p className="mt-1 line-clamp-2 text-xs text-gray-500">{description}</p>

          <div className="mt-2.5 flex flex-wrap items-center gap-1.5">
            <span
              className={`rounded-full px-2 py-0.5 text-[10px] font-medium ${ACHIEVEMENT_CATEGORY_STYLES[category]}`}
            >
              {ACHIEVEMENT_CATEGORY_LABELS[category]}
            </span>
            <span
              className="rounded-full px-2 py-0.5 text-[10px] font-semibold"
              style={{ backgroundColor: `${color}1a`, color }}
            >
              +{xpReward} XP
            </span>
          </div>
        </div>
      </div>

      {/* Progress section */}
      <div className="mt-4">
        {unlocked ? (
          <div className="flex items-center justify-between text-xs">
            <span className="flex items-center gap-1 font-medium text-amber-600">
              <Sparkles className="h-3.5 w-3.5" />
              Unlocked {achievement.unlockedAt ? formatDate(achievement.unlockedAt) : ''}
            </span>
            <span className="font-semibold text-gray-700">
              {achievement.targetValue} / {achievement.targetValue}
            </span>
          </div>
        ) : (
          <div className="flex items-center justify-between text-xs text-gray-500">
            <span className="truncate">
              Progress {achievement.progress} / {achievement.targetValue}
            </span>
            <span className="flex-shrink-0 font-semibold">
              {achievement.progressPercent}%
            </span>
          </div>
        )}

        <div className="mt-1.5 h-2 overflow-hidden rounded-full bg-gray-100">
          <div
            className={`h-full rounded-full xp-bar-fill ${
              unlocked ? 'xp-bar-shimmer' : ''
            }`}
            style={{
              width: `${achievement.progressPercent}%`,
              background: unlocked ? color : '#cbd5e1',
            }}
          />
        </div>
      </div>

      {/* Unlocked glow */}
      {unlocked && (
        <div
          className="pointer-events-none absolute -right-6 -top-6 h-20 w-20 rounded-full opacity-20 blur-2xl"
          style={{ backgroundColor: color }}
        />
      )}
    </div>
  );
};
