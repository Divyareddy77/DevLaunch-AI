package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.InterviewDifficulty;
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
 * Summarises a completed interview for the history list — session
 * identifier, category, difficulty, configuration, completion time,
 * overall and per-dimension scores — and carries the full snapshotted
 * report (strengths, improvements, suggestions, per-question feedback)
 * so the client can render the original report without extra requests.
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
     * The difficulty mode of the session, or {@code null} for legacy
     * sessions.
     */
    private InterviewDifficulty difficulty;

    /**
     * Whether the session was timed, or {@code null} for legacy sessions.
     */
    private Boolean timed;

    /**
     * The total time spent on the interview in seconds, or {@code null}
     * for legacy sessions.
     */
    private Integer durationSeconds;

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
     * The total number of words across all answers, or {@code null} for
     * legacy sessions.
     */
    private Integer wordCount;

    /**
     * The technical knowledge score (0–100), or {@code null} for legacy
     * sessions.
     */
    private Integer technicalScore;

    /**
     * The communication score (0–100), or {@code null} for legacy sessions.
     */
    private Integer communicationScore;

    /**
     * The confidence estimate (0–100), or {@code null} for legacy sessions.
     */
    private Integer confidenceScore;

    /**
     * The problem-solving score (0–100), or {@code null} for legacy
     * sessions.
     */
    private Integer problemSolvingScore;

    /**
     * The answer clarity score (0–100), or {@code null} for legacy sessions.
     */
    private Integer clarityScore;

    /**
     * The vocabulary breadth score (0–100), or {@code null} for legacy
     * sessions.
     */
    private Integer vocabularyScore;

    /**
     * The professionalism score (0–100), or {@code null} for legacy
     * sessions.
     */
    private Integer professionalismScore;

    /**
     * The strengths identified by the evaluation.
     */
    private List<String> strengths;

    /**
     * The areas for improvement identified by the evaluation.
     */
    private List<String> areasForImprovement;

    /**
     * The personalised practice suggestions for this session.
     */
    private List<String> suggestions;

    /**
     * The exact questions presented in the session with the user's answers
     * and evaluation, in the order they were answered, snapshotted so they
     * remain unchanged even if the question bank is later edited.
     */
    private List<MockInterviewQuestionResponse> questions;

}
