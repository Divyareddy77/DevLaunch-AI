package com.devlaunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A snapshot of a question exactly as it was presented in an interview
 * session.
 * <p>
 * The question text is copied from the question bank when the session is
 * submitted, so later additions, edits, or deletions of bank questions
 * never alter past interview history. The embedded question identifier is
 * kept for traceability back to the bank entry that was originally served.
 * </p>
 *
 * @author DevLaunch
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSessionQuestion {

    /**
     * The identifier of the bank question served in the session.
     * <p>
     * Must not be blank.
     * </p>
     */
    @Column(name = "question_id", nullable = false, length = 64)
    private String questionId;

    /**
     * The exact question text presented to the user.
     * <p>
     * Must not be blank.
     * </p>
     */
    @Column(name = "question", nullable = false, length = 500)
    private String question;

}
