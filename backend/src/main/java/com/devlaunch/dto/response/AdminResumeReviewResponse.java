package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for an AI resume review history record as seen by an
 * administrator.
 * <p>
 * Summarises who reviewed which resume, the optional target role, the two
 * scores produced, and when the review happened.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResumeReviewResponse {

    private Long id;

    private Long resumeId;

    private String resumeTitle;

    private String targetRole;

    private Integer resumeScore;

    private Integer atsScore;

    private Long userId;

    private String userEmail;

    private String userName;

    private LocalDateTime createdAt;

}
