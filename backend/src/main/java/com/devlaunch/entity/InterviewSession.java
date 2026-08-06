package com.devlaunch.entity;

import com.devlaunch.entity.enums.InterviewDifficulty;
import com.devlaunch.entity.enums.InterviewType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.BatchSize;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a completed mock interview session tracked for a registered user.
 * <p>
 * Each record captures the client-generated session identifier, the interview
 * category, the session configuration (difficulty, timed, duration), the
 * overall and per-dimension scores achieved, and a snapshot of the questions
 * and answers. A user may have many interview sessions, forming their
 * practice history and analytics.
 * </p>
 *
 * @author DevLaunch
 */
@Entity
@Table(name = "interview_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"user", "questions", "strengths",
        "areasForImprovement", "suggestions"})
@EqualsAndHashCode(callSuper = true, exclude = {"user", "questions", "strengths",
        "areasForImprovement", "suggestions"})
public class InterviewSession extends BaseEntity {

    /**
     * The client-generated session identifier returned when the interview
     * was started.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Session id is required")
    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    /**
     * The category of this interview session.
     * <p>
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Interview type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", nullable = false, length = 20)
    private InterviewType interviewType;

    /**
     * The overall score achieved across the interview, ranging from 0 to 100.
     */
    @Column(name = "overall_score", nullable = false)
    private int overallScore;

    /**
     * The number of questions answered during the interview.
     */
    @Column(name = "question_count", nullable = false)
    private int questionCount;

    /**
     * The difficulty mode of this session, or {@code null} for sessions
     * completed before difficulty selection was introduced.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 10)
    private InterviewDifficulty difficulty;

    /**
     * Whether this session was timed, or {@code null} for legacy sessions.
     */
    @Column(name = "timed")
    private Boolean timed;

    /**
     * The total time spent on the interview in seconds, or {@code null}
     * for legacy sessions.
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /**
     * The total number of words typed or spoken across all answers, or
     * {@code null} for legacy sessions.
     */
    @Column(name = "word_count")
    private Integer wordCount;

    /**
     * The technical knowledge score (0–100), or {@code null} for legacy
     * sessions.
     */
    @Column(name = "technical_score")
    private Integer technicalScore;

    /**
     * The communication score (0–100), or {@code null} for legacy sessions.
     */
    @Column(name = "communication_score")
    private Integer communicationScore;

    /**
     * The confidence estimate (0–100), or {@code null} for legacy sessions.
     */
    @Column(name = "confidence_score")
    private Integer confidenceScore;

    /**
     * The problem-solving score (0–100), or {@code null} for legacy sessions.
     */
    @Column(name = "problem_solving_score")
    private Integer problemSolvingScore;

    /**
     * The answer clarity score (0–100), or {@code null} for legacy sessions.
     */
    @Column(name = "clarity_score")
    private Integer clarityScore;

    /**
     * The vocabulary breadth score (0–100), or {@code null} for legacy
     * sessions.
     */
    @Column(name = "vocabulary_score")
    private Integer vocabularyScore;

    /**
     * The professionalism score (0–100), or {@code null} for legacy sessions.
     */
    @Column(name = "professionalism_score")
    private Integer professionalismScore;

    /**
     * The date and time the interview was completed.
     * <p>
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Completed at is required")
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    /**
     * The user who completed this interview session.
     * <p>
     * A user may have many interview sessions. Each session must be
     * associated with exactly one user.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The exact questions presented during this session, snapshotted at
     * submission time in the order they were answered together with the
     * user's answer, its score, and the written feedback.
     * <p>
     * The question text is copied from the bank when the session is
     * submitted, so later additions, edits, or deletions of bank
     * questions never alter past interview history.
     * </p>
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "interview_session_questions",
            joinColumns = @JoinColumn(name = "interview_session_id"))
    @OrderColumn(name = "question_order")
    @BatchSize(size = 25)
    @Builder.Default
    private List<InterviewSessionQuestion> questions = new ArrayList<>();

    /**
     * The strengths identified by the evaluation, snapshotted so the
     * history can always render the original report.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "interview_session_strengths",
            joinColumns = @JoinColumn(name = "interview_session_id"))
    @Column(name = "strength", length = 1000)
    @BatchSize(size = 25)
    @Builder.Default
    private List<String> strengths = new ArrayList<>();

    /**
     * The areas for improvement identified by the evaluation, snapshotted
     * so the history can always render the original report.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "interview_session_improvements",
            joinColumns = @JoinColumn(name = "interview_session_id"))
    @Column(name = "improvement", length = 1000)
    @BatchSize(size = 25)
    @Builder.Default
    private List<String> areasForImprovement = new ArrayList<>();

    /**
     * The personalised practice suggestions for this session, snapshotted
     * so the history can always render the original report.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "interview_session_suggestions",
            joinColumns = @JoinColumn(name = "interview_session_id"))
    @Column(name = "suggestion", length = 1000)
    @BatchSize(size = 25)
    @Builder.Default
    private List<String> suggestions = new ArrayList<>();

}
