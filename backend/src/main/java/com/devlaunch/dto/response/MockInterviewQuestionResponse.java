package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for a single mock interview question.
 * <p>
 * Contains the question identifier, the question text, and an optional
 * hint to help the user structure their answer.
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

}
