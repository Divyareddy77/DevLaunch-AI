package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for scheduling a new interview on a job application.
 * <p>
 * Captures the interview title, round, date, time, meeting link,
 * interviewer, and preparation notes. Only the title and date are
 * required — everything else is optional.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleInterviewRequest {

    /**
     * The title of the interview (e.g. "Technical Interview").
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Interview title is required")
    private String title;

    /**
     * The round of the interview (e.g. "Round 1", "HR").
     */
    private String round;

    /**
     * The calendar date of the interview.
     * <p>
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Interview date is required")
    private LocalDate scheduledDate;

    /**
     * The time of day the interview is scheduled for, if known.
     */
    private LocalTime scheduledTime;

    /**
     * The meeting link for the interview (e.g. Google Meet, Zoom).
     */
    private String meetingLink;

    /**
     * The name of the interviewer, if known.
     */
    private String interviewer;

    /**
     * Preparation notes or reminders for this interview.
     */
    private String notes;

}
