package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Response DTO for project information.
 * <p>
 * Exposes project data including the project name, description, technologies,
 * and optional links. Internal fields such as the resume association and
 * timestamps are excluded from the response.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    private Long id;

    private String projectName;

    private String description;

    private String technologies;

    private String githubUrl;

    private String liveUrl;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean currentlyWorking;

}
