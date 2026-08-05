package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for a user feedback entry as seen by an administrator.
 * <p>
 * Shows who submitted the feedback, the message, and when it was received.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminFeedbackResponse {

    private Long id;

    private String message;

    private Long userId;

    private String userEmail;

    private String userName;

    private LocalDateTime createdAt;

}
