package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.InterviewDifficulty;
import com.devlaunch.entity.enums.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO returned after interview answers are submitted and analysed.
 * <p>
 * Contains the overall interview score (0–100), a per-dimension breakdown
 * (technical, communication, confidence, problem solving, clarity,
 * vocabulary, professionalism), the per-question feedback, strengths,
 * areas for improvement, personalised suggestions, and the missed
 * concepts, together with the session metadata needed by the report.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewFeedbackResponse {

    /**
     * The session identifier this feedback belongs to.
     */
    private String sessionId;

    /**
     * The category of the interview that was analysed.
     */
    private InterviewType interviewType;

    /**
     * The difficulty mode of the session.
     */
    private InterviewDifficulty difficulty;

    /**
     * Whether the session was timed.
     */
    private Boolean timed;

    /**
     * The total time spent on the interview in seconds.
     */
    private Integer durationSeconds;

    /**
     * The total number of words across all answers.
     */
    private Integer wordCount;

    /**
     * The overall score for the entire interview, ranging from 0 to 100.
     */
    private Integer overallScore;

    /**
     * The technical knowledge score (0–100).
     */
    private Integer technicalScore;

    /**
     * The communication score (0–100).
     */
    private Integer communicationScore;

    /**
     * The confidence estimate (0–100).
     */
    private Integer confidenceScore;

    /**
     * The problem-solving score (0–100).
     */
    private Integer problemSolvingScore;

    /**
     * The answer clarity score (0–100).
     */
    private Integer clarityScore;

    /**
     * The vocabulary breadth score (0–100).
     */
    private Integer vocabularyScore;

    /**
     * The professionalism score (0–100).
     */
    private Integer professionalismScore;

    /**
     * The per-question feedback breakdown.
     */
    private List<MockInterviewFeedbackItemResponse> feedback;

    /**
     * The strengths identified across all answers.
     */
    private List<String> strengths;

    /**
     * The areas for improvement identified across all answers.
     */
    private List<String> areasForImprovement;

    /**
     * Personalised practice suggestions generated from the session.
     */
    private List<String> suggestions;

    /**
     * The key concepts the answers did not cover.
     */
    private List<String> missedConcepts;

}
