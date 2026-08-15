package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.AchievementCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A badge the user has unlocked, with its unlock timestamp.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnlockedAchievementResponse {

    private Long id;

    private String code;

    private AchievementCategory category;

    private String title;

    private String description;

    private String icon;

    private String color;

    /** The XP that was awarded when the badge unlocked. */
    private int xpReward;

    /** When the badge was unlocked. */
    private LocalDateTime unlockedAt;

}
