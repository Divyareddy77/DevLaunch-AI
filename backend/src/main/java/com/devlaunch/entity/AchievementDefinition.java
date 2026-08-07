package com.devlaunch.entity;

import com.devlaunch.entity.enums.AchievementCategory;
import com.devlaunch.entity.enums.ActivityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The static catalog entry of a gamification achievement.
 * <p>
 * One row per achievement badge (e.g. "ATS Expert", "Job Hunter"). Rows are
 * seeded on startup and shared by every user — per-user progress lives on
 * {@link UserAchievement}. The {@code code} column is unique and used by
 * the evaluation logic and the seed data; {@code activityType} declares
 * which platform activity feeds this badge (nullable for cross-cutting
 * achievements such as "Power User" that are evaluated on every event).
 * </p>
 *
 * @author DevLaunch
 */
@Entity
@Table(name = "achievement_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AchievementDefinition extends BaseEntity {

    /**
     * Stable machine-readable identifier used by the evaluator and the
     * seed data, e.g. {@code ATS_EXPERT}. Must be unique.
     */
    @NotBlank(message = "Code is required")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * The category this badge belongs to (used for grouping and filters).
     */
    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private AchievementCategory category;

    /**
     * The display title of the badge, e.g. "ATS Expert".
     */
    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * A short description of how the badge is unlocked.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * The emoji icon rendered on the badge card, e.g. "🏆".
     */
    @Column(name = "icon", length = 20)
    private String icon;

    /**
     * The accent color of the badge (hex), e.g. "#6366f1".
     */
    @Column(name = "color", length = 20)
    private String color;

    /**
     * The XP awarded the moment the badge unlocks.
     */
    @PositiveOrZero
    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    /**
     * The platform activity that feeds this badge, or {@code null} for
     * cross-cutting badges evaluated on every activity event.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", length = 40)
    private ActivityType activityType;

    /**
     * The progress value that must be reached to unlock the badge
     * (e.g. 90 for "ATS score &gt;= 90", 25 for "25 interviews").
     */
    @PositiveOrZero
    @Column(name = "target_value", nullable = false)
    private int targetValue;

    /**
     * Display ordering within the catalog.
     */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

}
