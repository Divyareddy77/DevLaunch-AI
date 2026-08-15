package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for submitting platform feedback.
 * <p>
 * Carries the feedback message written by the authenticated user. The
 * submitting user is resolved server-side.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackSubmissionRequest {

    /**
     * The feedback message.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Feedback message is required")
    @Size(max = 2000, message = "Feedback must not exceed 2000 characters")
    private String message;

}
