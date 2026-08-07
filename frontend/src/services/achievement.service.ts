/**
 * Gamification service.
 *
 * Provides methods for fetching the achievement catalog, the authenticated
 * user's badge progress, unlocks, XP ledger, and the aggregated summary.
 *
 * @see backend/src/main/java/com/devlaunch/controller/GamificationController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { ACHIEVEMENTS } from '../api/endpoints';
import type {
  Achievement,
  AchievementProgress,
  AchievementSummary,
  UnlockedAchievement,
  XpHistoryEntry,
} from '../types/achievement';

export const achievementService = {
  /**
   * Retrieves the full static achievement catalog.
   *
   * GET /api/achievements
   */
  getCatalog: () =>
    apiClient.get<Achievement[]>(ACHIEVEMENTS.BASE).then((res) => res.data),

  /**
   * Retrieves the badges the authenticated user has unlocked, newest first.
   *
   * GET /api/achievements/user
   */
  getUserAchievements: () =>
    apiClient.get<UnlockedAchievement[]>(ACHIEVEMENTS.USER).then((res) => res.data),

  /**
   * Retrieves the gamification summary: level, XP, next-level progress,
   * badge completion, and recent unlocks.
   *
   * GET /api/achievements/summary
   */
  getSummary: () =>
    apiClient.get<AchievementSummary>(ACHIEVEMENTS.SUMMARY).then((res) => res.data),

  /**
   * Retrieves the authenticated user's recent XP ledger, newest first.
   *
   * GET /api/achievements/history
   */
  getHistory: () =>
    apiClient.get<XpHistoryEntry[]>(ACHIEVEMENTS.HISTORY).then((res) => res.data),

  /**
   * Retrieves per-badge progress for the authenticated user.
   *
   * GET /api/achievements/progress
   */
  getProgress: () =>
    apiClient.get<AchievementProgress[]>(ACHIEVEMENTS.PROGRESS).then((res) => res.data),

};
