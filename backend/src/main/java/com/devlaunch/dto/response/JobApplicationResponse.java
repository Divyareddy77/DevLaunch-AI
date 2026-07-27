package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Response DTO for job application information.
 * <p>
 * Exposes job application data including the company name, job role,
 * status, and optional details. Internal fields such as the user
 * association and timestamps are excluded from the response.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicationResponse {

    private Long id;

    private String companyName;

    private String jobRole;

    private String companyLocation;

    private String jobType;

    private String salary;

    private LocalDate applicationDate;

    private ApplicationStatus status;

    private String jobUrl;

    private String notes;

    private Long resumeId;

}
