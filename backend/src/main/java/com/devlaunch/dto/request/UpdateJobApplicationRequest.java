package com.devlaunch.dto.request;

import com.devlaunch.entity.enums.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for updating an existing job application record.
 * <p>
 * Contains the updated job application details that the authenticated
 * user wishes to apply to an existing job application entry.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateJobApplicationRequest {

    /**
     * The updated name of the company to which the user applied.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Company name is required")
    private String companyName;

    /**
     * The updated title of the job role or position applied for.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Job role is required")
    private String jobRole;

    /**
     * The updated geographical location of the company or position
     * (e.g. city, state, country, or remote).
     */
    private String companyLocation;

    /**
     * The updated type of employment (e.g. Full-time, Part-time, Contract,
     * Freelance, Internship).
     */
    private String jobType;

    /**
     * The updated salary or compensation range associated with the position,
     * if known.
     */
    private String salary;

    /**
     * The updated date on which the application was submitted.
     */
    private LocalDate applicationDate;

    /**
     * The updated status of this job application within the hiring pipeline.
     * <p>
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Application status is required")
    private ApplicationStatus status;

    /**
     * The updated URL to the job posting or application page.
     */
    private String jobUrl;

    /**
     * Updated free-text notes about the application.
     */
    private String notes;

    /**
     * The updated ID of the resume used when submitting this
     * job application.
     * <p>
     * May be {@code null} if no specific resume was associated.
     * </p>
     */
    private Long resumeId;

}
