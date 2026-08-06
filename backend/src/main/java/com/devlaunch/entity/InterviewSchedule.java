package com.devlaunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a scheduled interview for a job application.
 * <p>
 * Captures everything a user needs for an upcoming interview: the round,
 * date and time, meeting link, interviewer, and preparation notes. An
 * interview may be marked as cancelled — cancelled interviews are hidden
 * from upcoming views but stay visible in the interview history so the
 * user can see what was rescheduled.
 * </p>
 */
@Entity
@Table(name = "interview_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "application")
@EqualsAndHashCode(callSuper = true)
public class InterviewSchedule extends BaseEntity {

    /**
     * The job application this interview belongs to.
     * <p>
     * An application may have many scheduled interviews (one per round).
     * </p>
     */
    @NotNull(message = "Application is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;

    /**
     * The title of the interview (e.g. "Technical Interview").
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Interview title is required")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * The round of the interview (e.g. "Round 1", "HR").
     */
    @Column(name = "round", length = 100)
    private String round;

    /**
     * The calendar date of the interview.
     * <p>
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Interview date is required")
    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    /**
     * The time of day the interview is scheduled for, if known.
     */
    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    /**
     * The meeting link (e.g. Google Meet, Zoom) for the interview.
     */
    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    /**
     * The name of the interviewer, if known.
     */
    @Column(name = "interviewer", length = 255)
    private String interviewer;

    /**
     * Preparation notes or reminders for this interview.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Whether this interview has been cancelled.
     * <p>
     * Defaults to {@code false}. Cancelled interviews are excluded from
     * upcoming views but remain in the history.
     * </p>
     */
    @Column(name = "cancelled", nullable = false)
    @Builder.Default
    private Boolean cancelled = Boolean.FALSE;

}
