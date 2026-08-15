package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response DTO for the next upcoming interview shown on the dashboard
 * widget.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingInterviewResponse {

    private Long applicationId;

    private String companyName;

    private String jobRole;

    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

}
