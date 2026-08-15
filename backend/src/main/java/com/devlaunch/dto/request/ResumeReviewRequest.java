package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for AI-powered resume review.
 * <p>
 * Identifies an existing resume owned by the authenticated user that
 * should be analysed, along with an optional target job role used to
 * tailor the review towards the user's career goal.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeReviewRequest {

    /**
     * The ID of the resume to review.
     * <p>
     * The resume must belong to the currently authenticated user.
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Resume id is required")
    private Long resumeId;

    /**
     * The optional target job role to tailor the review towards.
     * <p>
     * May be {@code null} if the user does not wish to target a
     * specific role. When provided it is used to detect missing
     * in-demand skills and keyword alignment.
     * </p>
     */
    @Size(max = 100, message = "Target role must be at most 100 characters")
    private String targetRole;

}
