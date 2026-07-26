package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for resume information.
 * <p>
 * Exposes resume data including the user's professional headline,
 * summary, and links to their online profiles.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeResponse {

    private Long id;

    private String headline;

    private String summary;

    private String linkedinUrl;

    private String githubUrl;

    private String portfolioUrl;

}
