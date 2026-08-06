package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response DTO for a scheduled interview on a job application.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewScheduleResponse {

    private Long id;

    private String title;

    private String round;

    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

    private String meetingLink;

    private String interviewer;

    private String notes;

    private Boolean cancelled;

}
