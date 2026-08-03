package com.devlaunch.dto.response;

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
 * Contains the overall interview score (0–100), a per-question feedback
 * breakdown, the strengths identified across all answers, and the areas
 * for improvement.
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
     * The overall score for the entire interview, ranging from 0 to 100.
     */
    private Integer overallScore;

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

}
