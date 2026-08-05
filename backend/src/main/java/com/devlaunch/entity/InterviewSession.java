package com.devlaunch.entity;

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
 * category, the overall score achieved, the number of questions answered, and
 * the time the interview was completed. A user may have many interview
 * sessions, forming their practice history.
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
@ToString(callSuper = true, exclude = {"user", "questions"})
@EqualsAndHashCode(callSuper = true, exclude = {"user", "questions"})
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
     * submission time in the order they were answered.
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
    @Builder.Default
    private List<InterviewSessionQuestion> questions = new ArrayList<>();

}
