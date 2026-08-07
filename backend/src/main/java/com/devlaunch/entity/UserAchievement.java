package com.devlaunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * The per-user record of an unlocked achievement badge.
 * <p>
 * One row is created the first time a user meets an achievement's unlock
 * condition. The unique constraint on {@code (user_id, achievement_id)}
 * is the hard guarantee that a badge can never be unlocked twice — the
 * achievement consumer also checks before inserting, so the constraint is
 * the final safety net against concurrent or re-delivered messages.
 * </p>
 *
 * @author DevLaunch
 */
@Entity
@Table(name = "user_achievements",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_achievements_user_achievement",
                columnNames = {"user_id", "achievement_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"user", "achievementDefinition"})
@EqualsAndHashCode(callSuper = true, exclude = {"user", "achievementDefinition"})
public class UserAchievement extends BaseEntity {

    /**
     * The user who unlocked the badge.
     */
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The badge that was unlocked.
     */
    @NotNull(message = "Achievement definition is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private AchievementDefinition achievementDefinition;

    /**
     * The moment the badge was unlocked. Stored explicitly (rather than
     * reusing {@code createdAt}) so it can be set inside the consumer's
     * transaction and remains stable even if the row is re-saved.
     */
    @NotNull(message = "Unlocked at is required")
    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;

}
