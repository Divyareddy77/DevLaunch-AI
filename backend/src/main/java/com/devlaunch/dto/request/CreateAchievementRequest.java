package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for creating a new achievement record.
 * <p>
 * Contains the achievement details required to add an achievement entry
 * to a specific resume owned by the authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAchievementRequest {

    /**
     * The title or name of the achievement.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Title is required")
    private String title;

    /**
     * A free-text description providing additional details about
     * the achievement, such as context, impact, or recognition received.
     */
    private String description;

    /**
     * The date on which the achievement was attained or recognised.
     */
    private LocalDate dateAchieved;

}
