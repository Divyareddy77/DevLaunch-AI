/**
 * AchievementsPage — the gamification hub.
 *
 * Renders the user's level and XP progression, badge completion stats,
 * a category-filterable badge grid with per-badge progress, a recent
 * unlock timeline, and the XP ledger. Detects newly unlocked badges
 * between page loads and celebrates them with a confetti popup.
 *
 * @see backend/src/main/java/com/devlaunch/controller/GamificationController.java
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Trophy, Zap, Medal, Target, Star } from 'lucide-react';
import { achievementService } from '../../services/achievement.service';
import {
  ACHIEVEMENT_CATEGORIES,
  ACHIEVEMENT_CATEGORY_LABELS,
  type AchievementCategory,
  type AchievementProgress,
  type AchievementSummary,
  type UnlockedAchievement,
  type XpHistoryEntry,
} from '../../types/achievement';
import { LevelCard } from '../../components/achievements/LevelCard';
import { AchievementCard } from '../../components/achievements/AchievementCard';
import { ProgressRing } from '../../components/achievements/ProgressRing';
import { RecentUnlockTimeline } from '../../components/achievements/RecentUnlockTimeline';
import { UnlockPopup } from '../../components/achievements/UnlockPopup';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { formatDate } from '../../utils/date';

/** Session-storage key holding the badge codes already celebrated. */
const SEEN_UNLOCKS_KEY = 'devlaunch.seen-unlocks';

/** Reads the set of already-celebrated badge codes. */
function readSeenUnlocks(): Set<string> {
  try {
    const raw = window.sessionStorage.getItem(SEEN_UNLOCKS_KEY);
    return raw ? new Set<string>(JSON.parse(raw) as string[]) : new Set<string>();
  } catch {
    return new Set<string>();
  }
}

/** Persists the set of already-celebrated badge codes. */
function writeSeenUnlocks(codes: Set<string>): void {
  try {
    window.sessionStorage.setItem(SEEN_UNLOCKS_KEY, JSON.stringify([...codes]));
  } catch {
    // Session storage unavailable — popup detection simply restarts.
  }
}

/** Animated count-up number. */
const AnimatedNumber: React.FC<{ value: number; duration?: number }> = ({
  value,
  duration = 900,
}) => {
  const [display, setDisplay] = React.useState(0);

  useEffect(() => {
    let frame = 0;
    const start = performance.now();
    const tick = (now: number) => {
      const progress = Math.min(1, (now - start) / duration);
      const eased = 1 - Math.pow(1 - progress, 3);
      setDisplay(Math.round(eased * value));
      if (progress < 1) {
        frame = requestAnimationFrame(tick);
      }
    };
    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
  }, [value, duration]);

  return <>{display.toLocaleString()}</>;
};

export const AchievementsPage: React.FC = () => {
  const [summary, setSummary] = useState<AchievementSummary | null>(null);
  const [progress, setProgress] = useState<AchievementProgress[]>([]);
  const [history, setHistory] = useState<XpHistoryEntry[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeCategory, setActiveCategory] = useState<AchievementCategory | 'ALL'>('ALL');
  const [celebrating, setCelebrating] = useState<UnlockedAchievement | null>(null);
  const seenRef = useRef<Set<string>>(readSeenUnlocks());

  const fetchAll = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [summaryData, progressData, historyData] = await Promise.all([
        achievementService.getSummary(),
        achievementService.getProgress(),
        achievementService.getHistory(),
      ]);
      setSummary(summaryData);
      setProgress(progressData);
      setHistory(historyData);

      // Detect badges unlocked since the last visit and celebrate the newest.
      const fresh = progressData.filter(
        (item) => item.unlocked && !seenRef.current.has(item.code),
      );
      if (fresh.length > 0) {
        const newest = fresh.reduce((a, b) =>
          (a.unlockedAt ?? '') > (b.unlockedAt ?? '') ? a : b,
        );
        fresh.forEach((item) => seenRef.current.add(item.code));
        writeSeenUnlocks(seenRef.current);
        setCelebrating({
          id: newest.id,
          code: newest.code,
          category: newest.category,
          title: newest.title,
          description: newest.description,
          icon: newest.icon,
          color: newest.color,
          xpReward: newest.xpReward,
          unlockedAt: newest.unlockedAt ?? new Date().toISOString(),
        });
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load achievements.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  const filtered = useMemo(
    () =>
      activeCategory === 'ALL'
        ? progress
        : progress.filter((item) => item.category === activeCategory),
    [progress, activeCategory],
  );

  const unlockedCount = summary?.unlockedCount ?? 0;
  const xpHistoryHeadline = history[0];

  if (isLoading) {
    return <LoadingScreen />;
  }

  if (error || !summary) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <ErrorMessage message={error ?? 'Unable to load achievements.'} onRetry={fetchAll} />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl">
      {/* Page header */}
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold text-gray-900 sm:text-3xl">
            <Trophy className="h-7 w-7 text-amber-500" />
            Achievements
          </h1>
          <p className="mt-1 text-sm text-gray-500">
            Earn XP and unlock badges by using every part of DevLaunch.
          </p>
        </div>
        <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-semibold text-amber-700">
          {unlockedCount} of {summary.totalAchievements} badges unlocked
        </span>
      </div>

      {/* Hero level card */}
      <LevelCard summary={summary} />

      {/* Stats row */}
      <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {/* Total XP */}
        <div className="flex items-center gap-4 rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-amber-100">
            <Zap className="h-5 w-5 text-amber-600" />
          </div>
          <div>
            <p className="text-xs font-medium uppercase tracking-wide text-gray-400">
              Total XP
            </p>
            <p className="text-2xl font-bold text-gray-900">
              <AnimatedNumber value={summary.totalXp} />
            </p>
          </div>
        </div>

        {/* Unlocked badges */}
        <div className="flex items-center gap-4 rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-indigo-100">
            <Medal className="h-5 w-5 text-indigo-600" />
          </div>
          <div>
            <p className="text-xs font-medium uppercase tracking-wide text-gray-400">
              Unlocked
            </p>
            <p className="text-2xl font-bold text-gray-900">
              <AnimatedNumber value={summary.unlockedCount} />
              <span className="ml-1 text-sm font-medium text-gray-400">
                / {summary.totalAchievements}
              </span>
            </p>
          </div>
        </div>

        {/* Completion */}
        <div className="flex items-center gap-4 rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
          <ProgressRing percent={summary.completionPercent} size={52} strokeWidth={6} color="#10b981">
            <Star className="h-5 w-5 text-emerald-500" />
          </ProgressRing>
          <div>
            <p className="text-xs font-medium uppercase tracking-wide text-gray-400">
              Completion
            </p>
            <p className="text-2xl font-bold text-gray-900">{summary.completionPercent}%</p>
          </div>
        </div>

        {/* Next level */}
        <div className="flex items-center gap-4 rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-violet-100">
            <Target className="h-5 w-5 text-violet-600" />
          </div>
          <div>
            <p className="text-xs font-medium uppercase tracking-wide text-gray-400">
              Next level
            </p>
            <p className="text-2xl font-bold text-gray-900">
              Lvl {summary.nextLevel}
              <span className="ml-1 text-sm font-medium text-gray-400">
                · {summary.xpNeededForNext} XP
              </span>
            </p>
          </div>
        </div>
      </div>

      {/* Main grid: badges + timeline */}
      <div className="mt-8 grid gap-6 lg:grid-cols-3">
        {/* Badge grid */}
        <div className="lg:col-span-2">
          {/* Category filters */}
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => setActiveCategory('ALL')}
              className={`rounded-full px-3.5 py-1.5 text-xs font-semibold transition-all ${
                activeCategory === 'ALL'
                  ? 'bg-gray-900 text-white shadow'
                  : 'bg-white text-gray-600 hover:bg-gray-100'
              }`}
            >
              All
            </button>
            {ACHIEVEMENT_CATEGORIES.map((category) => {
              const count = progress.filter((item) => item.category === category).length;
              return (
                <button
                  key={category}
                  onClick={() => setActiveCategory(category)}
                  className={`rounded-full px-3.5 py-1.5 text-xs font-semibold transition-all ${
                    activeCategory === category
                      ? 'bg-gray-900 text-white shadow'
                      : 'bg-white text-gray-600 hover:bg-gray-100'
                  }`}
                >
                  {ACHIEVEMENT_CATEGORY_LABELS[category]}
                  <span className="ml-1 opacity-60">{count}</span>
                </button>
              );
            })}
          </div>

          {/* Badge cards */}
          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            {filtered.map((achievement, index) => (
              <AchievementCard key={achievement.code} achievement={achievement} delay={index * 50} />
            ))}
          </div>

          {filtered.length === 0 && (
            <p className="mt-8 text-center text-sm text-gray-400">
              No badges in this category yet.
            </p>
          )}

          {/* XP ledger */}
          <div className="mt-8 rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
            <h3 className="text-sm font-semibold text-gray-800">Recent XP</h3>
            <ul className="mt-4 space-y-3">
              {history.slice(0, 8).map((entry, index) => (
                <li
                  key={entry.id}
                  className="flex items-center gap-3 text-sm animate-fade-in-up"
                  style={{ animationDelay: `${index * 40}ms` }}
                >
                  <span
                    className={`flex h-7 w-7 flex-shrink-0 items-center justify-center rounded-lg text-xs font-bold ${
                      entry.reason === 'ACHIEVEMENT_UNLOCKED'
                        ? 'bg-amber-100 text-amber-700'
                        : 'bg-indigo-100 text-indigo-600'
                    }`}
                  >
                    +{entry.amount}
                  </span>
                  <span className="flex-1 truncate text-gray-700">{entry.description}</span>
                  <span className="flex-shrink-0 text-xs text-gray-400">
                    {formatDate(entry.createdAt)}
                  </span>
                </li>
              ))}
              {history.length === 0 && (
                <li className="text-sm text-gray-400">
                  Your XP ledger is empty — complete any activity to earn XP.
                </li>
              )}
            </ul>
          </div>
        </div>

        {/* Timeline sidebar */}
        <div className="space-y-6">
          <RecentUnlockTimeline unlocks={summary.recentUnlocks} />

          {xpHistoryHeadline && (
            <div className="rounded-2xl bg-gradient-to-br from-amber-400 to-orange-500 p-5 text-white shadow-md">
              <p className="text-xs font-semibold uppercase tracking-widest text-amber-100">
                Latest reward
              </p>
              <p className="mt-2 text-2xl font-bold">+{xpHistoryHeadline.amount} XP</p>
              <p className="mt-1 text-sm text-amber-50">{xpHistoryHeadline.description}</p>
            </div>
          )}
        </div>
      </div>

      {/* Unlock celebration popup */}
      {celebrating && (
        <UnlockPopup achievement={celebrating} onClose={() => setCelebrating(null)} />
      )}
    </div>
  );
};
