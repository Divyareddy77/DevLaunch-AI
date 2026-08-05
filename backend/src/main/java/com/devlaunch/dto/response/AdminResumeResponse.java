package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for a resume as seen by an administrator.
 * <p>
 * Shows the resume content together with the owning user's identity and
 * audit timestamps so admins can review and moderate resumes.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResumeResponse {

    private Long id;

    private String headline;

    private String summary;

    private String linkedinUrl;

    private String githubUrl;

    private String portfolioUrl;

    private Long userId;

    private String userEmail;

    private String userName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
