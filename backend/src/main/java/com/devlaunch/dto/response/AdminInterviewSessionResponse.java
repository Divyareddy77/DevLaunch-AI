package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for a mock interview session as seen by an administrator.
 * <p>
 * Summarises who completed an interview, its category, the overall score,
 * the number of questions answered, and when it was completed. Admins use
 * this to monitor AI mock interview activity and moderate history.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminInterviewSessionResponse {

    private Long id;

    private String sessionId;

    private InterviewType interviewType;

    private Integer overallScore;

    private Integer questionCount;

    private LocalDateTime completedAt;

    private Long userId;

    private String userEmail;

    private String userName;

}
