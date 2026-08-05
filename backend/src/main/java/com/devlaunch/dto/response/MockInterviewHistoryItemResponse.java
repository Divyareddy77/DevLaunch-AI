package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for a single historical interview session.
 * <p>
 * Summarises a completed interview for the history list, including the
 * session identifier, category, completion time, overall score, and the
 * number of questions answered.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewHistoryItemResponse {

    /**
     * The session identifier of the completed interview.
     */
    private String sessionId;

    /**
     * The category of the interview.
     */
    private InterviewType interviewType;

    /**
     * The date and time the interview was completed.
     */
    private LocalDateTime completedAt;

    /**
     * The overall score achieved, ranging from 0 to 100.
     */
    private Integer overallScore;

    /**
     * The number of questions answered in the session.
     */
    private Integer questionCount;

    /**
     * The exact questions presented in the session, in the order they
     * were answered, snapshotted so they remain unchanged even if the
     * question bank is later edited.
     */
    private List<MockInterviewQuestionResponse> questions;

}
