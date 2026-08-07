package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * The gamification summary for the authenticated user.
 * <p>
 * Aggregates the level state (level, title, XP boundaries), the badge
 * catalog totals (total / unlocked / locked / completion percentage), and
 * the most recent unlocks for the timeline widget.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementSummaryResponse {

    /** The user's current level. */
    private int level;

    /** The display title of the current level, e.g. "Expert". */
    private String levelTitle;

    /** The user's total XP. */
    private long totalXp;

    /** The cumulative XP at which the current level starts. */
    private long currentLevelXp;

    /** The cumulative XP at which the next level starts. */
    private long nextLevelXp;

    /** The number of the next level. */
    private int nextLevel;

    /** XP earned within the current level. */
    private long xpIntoLevel;

    /** XP still needed to reach the next level. */
    private long xpNeededForNext;

    /** Progress within the current level (0–100). */
    private double levelProgressPercent;

    /** Total number of badges in the catalog. */
    private int totalAchievements;

    /** Number of unlocked badges. */
    private int unlockedCount;

    /** Number of locked badges. */
    private int lockedCount;

    /** Completion percentage of the badge catalog (0–100). */
    private int completionPercent;

    /** The most recent unlock, or {@code null} when nothing is unlocked yet. */
    private UnlockedAchievementResponse latestUnlock;

    /** The most recent unlocks, newest first (used by the timeline widget). */
    private List<UnlockedAchievementResponse> recentUnlocks;

}
