package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating a new resume.
 * <p>
 * Contains the professional details required to create a resume
 * for the authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateResumeRequest {

    /**
     * A short professional headline or tagline.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Headline is required")
    private String headline;

    /**
     * A detailed professional summary.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Summary is required")
    private String summary;

    /**
     * URL to the user's LinkedIn profile.
     */
    private String linkedinUrl;

    /**
     * URL to the user's GitHub profile.
     */
    private String githubUrl;

    /**
     * URL to the user's personal portfolio or website.
     */
    private String portfolioUrl;

}
