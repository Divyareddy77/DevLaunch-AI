package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for updating an existing achievement record.
 * <p>
 * Contains the updated achievement details that the authenticated
 * user wishes to apply to an existing achievement entry on their resume.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAchievementRequest {

    /**
     * The updated title or name of the achievement.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Title is required")
    private String title;

    /**
     * The updated free-text description of the achievement.
     */
    private String description;

    /**
     * The updated date on which the achievement was attained or recognised.
     */
    private LocalDate dateAchieved;

}
