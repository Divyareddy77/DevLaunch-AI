package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO representing a single question and the user's answer to it.
 * <p>
 * The question text is echoed back by the client so the AI evaluator can
 * score each answer without re-deriving the question, which keeps the
 * submission stateless across both deterministic and LLM-backed providers.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewAnswerRequest {

    /**
     * The identifier of the question within the interview session.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Question id is required")
    private String questionId;

    /**
     * The question text the user was asked.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Question is required")
    private String question;

    /**
     * The user's answer to the question.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Answer is required")
    private String answer;

}
