package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for a single mock interview question.
 * <p>
 * In a freshly started session only the identifier, question text, hint,
 * and optional difficulty are populated. When the same DTO is reused for
 * a historical session report, the user's answer, score, feedback,
 * suggestions, and improved answer are also populated.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewQuestionResponse {

    /**
     * The unique identifier of the question within the session.
     */
    private String id;

    /**
     * The question text presented to the user.
     */
    private String question;

    /**
     * An optional hint or context to help the user answer the question.
     * May be {@code null}.
     */
    private String hint;

    /**
     * The difficulty of the question, or {@code null} when the provider
     * does not expose per-question difficulty.
     */
    private Difficulty difficulty;

    /**
     * The user's submitted answer, populated only in history reports.
     */
    private String answer;

    /**
     * The per-question score (0–100), populated only in history reports.
     */
    private Integer score;

    /**
     * The written feedback, populated only in history reports.
     */
    private String feedback;

    /**
     * Specific suggestions for this answer, populated only in history
     * reports.
     */
    private List<String> suggestions;

    /**
     * A sample improved answer, populated only in history reports. May be
     * {@code null}.
     */
    private String improvedAnswer;

}
