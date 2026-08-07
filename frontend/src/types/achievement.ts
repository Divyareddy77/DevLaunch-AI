/**
 * Type definitions for the gamification module.
 *
 * Mirrors the backend DTOs in
 * com.devlaunch.dto.response.* (AchievementResponse, AchievementProgressResponse,
 * AchievementSummaryResponse, UnlockedAchievementResponse, XpHistoryResponse)
 * and the entity enums (AchievementCategory, XpReason).
 *
 * @see backend/src/main/java/com/devlaunch/controller/GamificationController.java
 * @author DevLaunch
 */

/** The achievement badge categories, matching the backend enum. */
export type AchievementCategory =
  | 'RESUME'
  | 'INTERVIEW'
  | 'JOB_TRACKER'
  | 'STUDY_PLANNER'
  | 'GITHUB'
  | 'LEETCODE'
  | 'PLACEMENT'
  | 'CONSISTENCY'
  | 'SPECIAL';

/** A static catalog entry of an achievement badge. */
export interface Achievement {
  id: number;
  /** Stable machine-readable code, e.g. 'ATS_EXPERT'. */
  code: string;
  category: AchievementCategory;
  title: string;
  description: string;
  icon: string;
  /** Accent colour of the badge (hex), e.g. '#6366f1'. */
  color: string;
  /** XP awarded when the badge unlocks. */
  xpReward: number;
  /** Progress value required to unlock the badge. */
  targetValue: number;
}

/** Per-user progress for a single badge. */
export interface AchievementProgress extends Achievement {
  /** The user's current progress towards the target. */
  progress: number;
  /** Whether the user already unlocked the badge. */
  unlocked: boolean;
  /** ISO-8601 unlock timestamp, or null while locked. */
  unlockedAt: string | null;
  /** Progress as a percentage of the target, capped at 100. */
  progressPercent: number;
}

/** A badge the user has unlocked, with its unlock timestamp. */
export interface UnlockedAchievement {
  id: number;
  code: string;
  category: AchievementCategory;
  title: string;
  description: string;
  icon: string;
  color: string;
  xpReward: number;
  /** ISO-8601 unlock timestamp. */
  unlockedAt: string;
}

/** Aggregated gamification summary for the authenticated user. */
export interface AchievementSummary {
  level: number;
  levelTitle: string;
  totalXp: number;
  currentLevelXp: number;
  nextLevelXp: number;
  nextLevel: number;
  xpIntoLevel: number;
  xpNeededForNext: number;
  levelProgressPercent: number;
  totalAchievements: number;
  unlockedCount: number;
  lockedCount: number;
  completionPercent: number;
  latestUnlock: UnlockedAchievement | null;
  recentUnlocks: UnlockedAchievement[];
}

/** Reasons an XP entry was awarded (matches the backend XpReason enum). */
export type XpReason =
  | 'RESUME_CREATED'
  | 'RESUME_REVIEWED'
  | 'JOB_APPLICATION_CREATED'
  | 'INTERVIEW_COMPLETED'
  | 'STUDY_TASK_COMPLETED'
  | 'GITHUB_CONNECTED'
  | 'LEETCODE_SYNCED'
  | 'PLACEMENT_UPDATED'
  | 'ACHIEVEMENT_UNLOCKED';

/** One entry of the user's XP ledger. */
export interface XpHistoryEntry {
  id: number;
  amount: number;
  reason: XpReason;
  description: string;
  /** ISO-8601 creation timestamp. */
  createdAt: string;
}

/** Human-readable labels for each badge category. */
export const ACHIEVEMENT_CATEGORY_LABELS: Record<AchievementCategory, string> = {
  RESUME: 'Resume',
  INTERVIEW: 'Interview',
  JOB_TRACKER: 'Job Tracker',
  STUDY_PLANNER: 'Study Planner',
  GITHUB: 'GitHub',
  LEETCODE: 'LeetCode',
  PLACEMENT: 'Placement',
  CONSISTENCY: 'Consistency',
  SPECIAL: 'Special',
};

/** All badge categories in display order, useful for filters. */
export const ACHIEVEMENT_CATEGORIES: AchievementCategory[] = [
  'RESUME',
  'INTERVIEW',
  'JOB_TRACKER',
  'STUDY_PLANNER',
  'GITHUB',
  'LEETCODE',
  'PLACEMENT',
  'CONSISTENCY',
  'SPECIAL',
];

/** Accent Tailwind classes per category (used for chips and icons). */
export const ACHIEVEMENT_CATEGORY_STYLES: Record<AchievementCategory, string> = {
  RESUME: 'bg-indigo-100 text-indigo-700',
  INTERVIEW: 'bg-violet-100 text-violet-700',
  JOB_TRACKER: 'bg-emerald-100 text-emerald-700',
  STUDY_PLANNER: 'bg-amber-100 text-amber-700',
  GITHUB: 'bg-slate-200 text-slate-700',
  LEETCODE: 'bg-yellow-100 text-yellow-700',
  PLACEMENT: 'bg-teal-100 text-teal-700',
  CONSISTENCY: 'bg-rose-100 text-rose-700',
  SPECIAL: 'bg-fuchsia-100 text-fuchsia-700',
};
