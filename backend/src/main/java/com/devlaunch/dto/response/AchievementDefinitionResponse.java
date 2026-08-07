package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.AchievementCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catalog entry of a gamification achievement badge.
 * <p>
 * Carries the static badge data shared by every user: identity, category,
 * display text, badge styling, the XP reward, and the unlock target.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementDefinitionResponse {

    private Long id;

    /** Stable machine-readable code, e.g. {@code ATS_EXPERT}. */
    private String code;

    /** The category the badge belongs to. */
    private AchievementCategory category;

    /** The display title of the badge. */
    private String title;

    /** A short description of how the badge is unlocked. */
    private String description;

    /** The emoji icon rendered on the badge card. */
    private String icon;

    /** The accent color of the badge (hex). */
    private String color;

    /** The XP awarded when the badge unlocks. */
    private int xpReward;

    /** The progress value required to unlock the badge. */
    private int targetValue;

}
