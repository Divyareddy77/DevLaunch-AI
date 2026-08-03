package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for the AI feedback on a single interview answer.
 * <p>
 * Contains the question that was asked, the user's answer, a per-answer
 * score (0–100), a written assessment, and specific suggestions for
 * improvement.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewFeedbackItemResponse {

    /**
     * The identifier of the question this feedback relates to.
     */
    private String questionId;

    /**
     * The original question text.
     */
    private String question;

    /**
     * The user's submitted answer.
     */
    private String answer;

    /**
     * A score from 0–100 for this answer.
     */
    private Integer score;

    /**
     * The written feedback for this answer.
     */
    private String feedback;

    /**
     * Specific suggestions to improve this answer.
     */
    private List<String> suggestions;

}
