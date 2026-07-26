package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for creating a new project record.
 * <p>
 * Contains the project details required to add a project entry
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
public class CreateProjectRequest {

    /**
     * The name of the project.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Project name is required")
    private String projectName;

    /**
     * A detailed description of the project, including its purpose,
     * features, and the user's role or contributions.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Description is required")
    private String description;

    /**
     * The technologies, frameworks, and tools used in the project.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Technologies are required")
    private String technologies;

    /**
     * The URL to the project's source code repository (e.g. GitHub, GitLab).
     */
    private String githubUrl;

    /**
     * The URL to the live deployment or demo of the project.
     */
    private String liveUrl;

    /**
     * The date on which the project was started.
     */
    private LocalDate startDate;

    /**
     * The date on which the project was completed.
     */
    private LocalDate endDate;

    /**
     * Indicates whether the project is currently ongoing.
     */
    private Boolean currentlyWorking;

}
