package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.AchievementCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Per-user progress for a single achievement badge.
 * <p>
 * Combines the static catalog data with the authenticated user's progress
 * towards the unlock target, the locked/unlocked status, and — when
 * unlocked — the exact unlock timestamp.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementProgressResponse {

    private Long id;

    private String code;

    private AchievementCategory category;

    private String title;

    private String description;

    private String icon;

    private String color;

    /** The XP awarded when the badge unlocks. */
    private int xpReward;

    /** The progress value required to unlock the badge. */
    private int targetValue;

    /** The user's current progress towards the target. */
    private int progress;

    /** Whether the user already unlocked the badge. */
    private boolean unlocked;

    /** The unlock timestamp, or {@code null} while locked. */
    private LocalDateTime unlockedAt;

    /** Progress as a percentage of the target, capped at 100. */
    private int progressPercent;

}
