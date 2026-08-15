package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for GitHub repository information.
 * <p>
 * Exposes public GitHub repository data fetched from the GitHub REST API,
 * including the repository name, description, primary language, star and
 * fork counts, the URL to the repository on GitHub, and its creation and
 * last-updated timestamps. Internal fields such as owner metadata and
 * raw API identifiers are excluded from the response.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryResponse {

    private String name;

    private String description;

    private String language;

    private Integer stars;

    private Integer forks;

    private String repositoryUrl;

    private String createdAt;

    private String updatedAt;

}
