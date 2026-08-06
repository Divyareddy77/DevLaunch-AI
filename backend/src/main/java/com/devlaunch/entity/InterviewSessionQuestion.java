package com.devlaunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A snapshot of a question exactly as it was presented in an interview
 * session, together with the user's answer and its evaluation.
 * <p>
 * The question text is copied from the question bank when the session is
 * submitted, so later additions, edits, or deletions of bank questions
 * never alter past interview history. The embedded question identifier is
 * kept for traceability back to the bank entry that was originally served,
 * and the answer, score, feedback, and improved answer are snapshotted so
 * the history can always render the original report.
 * </p>
 *
 * @author DevLaunch
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
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

    /**
     * The user's submitted answer, snapshotted for the history report.
     */
    @Column(name = "answer", length = 4000)
    private String answer;

    /**
     * The per-question score (0–100), snapshotted for the history report.
     */
    @Column(name = "score")
    private Integer score;

    /**
     * The written feedback for this answer, snapshotted for the history
     * report.
     */
    @Column(name = "feedback", length = 4000)
    private String feedback;

    /**
     * A sample improved answer, snapshotted for the history report. May be
     * {@code null} when the answer already covered the key concepts.
     */
    @Column(name = "improved_answer", length = 4000)
    private String improvedAnswer;

    /**
     * Backwards-compatible constructor for sessions that only snapshot the
     * question text (no answer or evaluation data).
     *
     * @param questionId the identifier of the bank question
     * @param question   the exact question text
     */
    public InterviewSessionQuestion(final String questionId, final String question) {
        this.questionId = questionId;
        this.question = question;
    }

    /**
     * Full constructor snapshotting the question, the user's answer, and
     * its evaluation.
     *
     * @param questionId     the identifier of the bank question
     * @param question       the exact question text
     * @param answer         the user's submitted answer
     * @param score          the per-question score (0–100)
     * @param feedback       the written feedback
     * @param improvedAnswer a sample improved answer, or {@code null}
     */
    public InterviewSessionQuestion(final String questionId, final String question,
                                    final String answer, final Integer score,
                                    final String feedback, final String improvedAnswer) {
        this.questionId = questionId;
        this.question = question;
        this.answer = answer;
        this.score = score;
        this.feedback = feedback;
        this.improvedAnswer = improvedAnswer;
    }

}
