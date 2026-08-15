/**
 * AchievementsWidget — the premium gamification widget.
 *
 * Shows the current level, total XP with an animated progress bar, the
 * latest badge with a glow animation, recently unlocked badges, and
 * overall achievement completion — with a button to the achievements page.
 *
 * @author DevLaunch-AI
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Trophy, Zap, ArrowRight, Award } from 'lucide-react';
import { AnalyticsCard } from '../analytics/AnalyticsCard';
import { CountUp } from '../../ui/CountUp';
import { formatDate } from '../../../utils/date';
import { ROUTES } from '../../../constants/routes';
import type { AchievementSummary } from '../../../types/achievement';

interface AchievementsWidgetProps {
  /** The gamification summary, or null while loading/unavailable. */
  summary: AchievementSummary | null;
  /** Whether the summary is still loading. */
  loading?: boolean;
}

export const AchievementsWidget: React.FC<AchievementsWidgetProps> = ({
  summary,
  loading = summary === null,
}) => {
  const navigate = useNavigate();

  return (
    <AnalyticsCard
      title="Achievements"
      subtitle={summary ? `Level ${summary.level} · ${summary.levelTitle}` : 'Gamification progress'}
      icon={<Trophy className="h-5 w-5" />}
      tone="gold"
      loading={loading}
      action={
        <button
          type="button"
          onClick={() => navigate(ROUTES.ACHIEVEMENTS)}
          className="inline-flex items-center gap-1 text-xs font-medium text-indigo-600 transition-colors hover:text-indigo-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-1"
        >
          View All
          <ArrowRight className="h-3 w-3" />
        </button>
      }
    >
      {!summary ? (
        <div className="flex flex-col items-center py-6 text-center">
          <Trophy className="mb-2 h-8 w-8 text-gray-300" />
          <p className="text-sm font-medium text-gray-500">Start your journey</p>
          <p className="mt-0.5 text-xs text-gray-400">
            Complete tasks across modules to earn achievements
          </p>
        </div>
      ) : (
        <div>
          {/* XP + level progress */}
          <div className="flex items-baseline gap-1.5">
            <Zap className="h-4 w-4 self-center text-amber-500" />
            <CountUp
              value={summary.totalXp}
              className="text-2xl font-bold tracking-tight text-gray-900"
            />
            <span className="text-xs text-gray-400">XP</span>
            <span className="ml-auto rounded-full bg-amber-50 px-2 py-0.5 text-[11px] font-semibold text-amber-700">
              {summary.unlockedCount}/{summary.totalAchievements} badges
            </span>
          </div>

          {/* Level progress bar */}
          <div className="mt-3">
            <div className="flex items-center justify-between text-[11px] text-gray-400">
              <span>
                {summary.xpIntoLevel} / {summary.nextLevelXp - summary.currentLevelXp} XP
              </span>
              <span>Level {summary.nextLevel}</span>
            </div>
            <div className="mt-1 h-2 overflow-hidden rounded-full bg-gray-100">
              <div
                className="xp-bar-fill h-full overflow-hidden rounded-full bg-gradient-to-r from-amber-400 to-orange-500"
                style={{ width: `${Math.min(100, Math.max(0, summary.levelProgressPercent))}%` }}
              >
                <div className="xp-bar-shimmer h-full w-1/3 bg-gradient-to-r from-transparent via-white/60 to-transparent" />
              </div>
            </div>
          </div>

          {/* Latest badge with glow */}
          <div className="mt-4 flex items-center gap-3 rounded-xl border border-amber-100 bg-gradient-to-br from-amber-50/80 to-white p-3">
            {summary.latestUnlock ? (
              <>
                <span
                  className="animate-badge-glow flex h-11 w-11 shrink-0 items-center justify-center rounded-xl text-xl"
                  style={{ backgroundColor: `${summary.latestUnlock.color}1f` }}
                  title={summary.latestUnlock.title}
                >
                  {summary.latestUnlock.icon}
                </span>
                <div className="min-w-0 flex-1">
                  <p className="text-xs font-semibold text-gray-800">
                    {summary.latestUnlock.title}
                  </p>
                  <p className="text-[11px] text-gray-400">
                    {summary.latestUnlock.unlockedAt
                      ? `Unlocked ${formatDate(summary.latestUnlock.unlockedAt)}`
                      : 'Latest badge'}
                  </p>
                </div>
                <span className="shrink-0 rounded-full bg-amber-100 px-2 py-0.5 text-[10px] font-bold text-amber-700">
                  +{summary.latestUnlock.xpReward} XP
                </span>
              </>
            ) : (
              <p className="text-xs text-gray-400">No badges yet — keep exploring!</p>
            )}
          </div>

          {/* Recently unlocked */}
          {summary.recentUnlocks.length > 0 && (
            <div className="mt-3">
              <p className="mb-1.5 text-[10px] font-semibold uppercase tracking-wide text-gray-400">
                Recently Unlocked
              </p>
              <div className="flex flex-wrap gap-1.5">
                {summary.recentUnlocks.slice(0, 3).map((badge) => (
                  <span
                    key={badge.id}
                    className="inline-flex items-center gap-1.5 rounded-full border border-gray-100 bg-gray-50/80 py-1 pl-1 pr-2.5 text-[11px] font-medium text-gray-600"
                    title={`${badge.title} — ${badge.description}`}
                  >
                    <span
                      className="flex h-5 w-5 items-center justify-center rounded-full text-[10px]"
                      style={{ backgroundColor: `${badge.color}1f` }}
                    >
                      {badge.icon}
                    </span>
                    <span className="max-w-[90px] truncate">{badge.title}</span>
                  </span>
                ))}
                <span className="inline-flex items-center gap-1 py-1 text-[11px] font-medium text-amber-600">
                  <Award className="h-3.5 w-3.5" />
                  {summary.completionPercent}% complete
                </span>
              </div>
            </div>
          )}
        </div>
      )}
    </AnalyticsCard>
  );
};
