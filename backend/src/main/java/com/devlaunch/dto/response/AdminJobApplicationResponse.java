package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for a job application as seen by an administrator.
 * <p>
 * Shows the application details together with the owning user's identity
 * so admins can monitor the job search activity across the platform.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminJobApplicationResponse {

    private Long id;

    private String companyName;

    private String jobRole;

    private String companyLocation;

    private String jobType;

    private String salary;

    private ApplicationStatus status;

    private LocalDate applicationDate;

    private Long userId;

    private String userEmail;

    private String userName;

    private LocalDateTime createdAt;

}
