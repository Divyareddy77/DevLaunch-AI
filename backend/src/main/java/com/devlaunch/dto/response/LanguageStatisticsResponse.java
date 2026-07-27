package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for GitHub language statistics information.
 * <p>
 * Exposes language usage statistics aggregated from a GitHub user's
 * repositories, including each language name and how many repositories
 * use that language as their primary language. This data is computed
 * live from the GitHub REST API and is not persisted locally.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageStatisticsResponse {

    private String language;

    private Long repositoryCount;

}
