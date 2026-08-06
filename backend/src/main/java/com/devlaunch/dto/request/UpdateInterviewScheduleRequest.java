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
 * Request DTO for updating or cancelling an existing interview schedule.
 * <p>
 * Carries the same fields as {@link ScheduleInterviewRequest} plus a
 * {@code cancelled} flag. When an interview transitions to cancelled the
 * service records a timeline event and notifies the user.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInterviewScheduleRequest {

    /**
     * The title of the interview.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Interview title is required")
    private String title;

    /**
     * The round of the interview.
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
     * The meeting link for the interview.
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

    /**
     * Whether the interview is cancelled.
     */
    private Boolean cancelled;

}
