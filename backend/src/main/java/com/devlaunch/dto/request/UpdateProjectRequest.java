package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for updating an existing project record.
 * <p>
 * Contains the updated project details that the authenticated
 * user wishes to apply to an existing project entry on their resume.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectRequest {

    /**
     * The updated name of the project.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Project name is required")
    private String projectName;

    /**
     * The updated description of the project.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Description is required")
    private String description;

    /**
     * The updated technologies used in the project.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Technologies are required")
    private String technologies;

    /**
     * The updated URL to the project's source code repository.
     */
    private String githubUrl;

    /**
     * The updated URL to the live deployment or demo of the project.
     */
    private String liveUrl;

    /**
     * The updated start date of the project.
     */
    private LocalDate startDate;

    /**
     * The updated end date of the project.
     */
    private LocalDate endDate;

    /**
     * Indicates whether the project is currently ongoing.
     */
    private Boolean currentlyWorking;

}
