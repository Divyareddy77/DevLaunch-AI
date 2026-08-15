package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating an existing resume.
 * <p>
 * Contains the updated professional details that the authenticated
 * user wishes to apply to their existing resume.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateResumeRequest {

    /**
     * The updated professional headline or tagline.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Headline is required")
    private String headline;

    /**
     * The updated professional summary.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Summary is required")
    private String summary;

    /**
     * Updated URL to the user's LinkedIn profile.
     */
    private String linkedinUrl;

    /**
     * Updated URL to the user's GitHub profile.
     */
    private String githubUrl;

    /**
     * Updated URL to the user's personal portfolio or website.
     */
    private String portfolioUrl;

}
