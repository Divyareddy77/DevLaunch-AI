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
 * Request DTO for creating a new job application record.
 * <p>
 * Contains the job application details required to track an
 * application submitted by the authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJobApplicationRequest {

    /**
     * The name of the company to which the user applied.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Company name is required")
    private String companyName;

    /**
     * The title of the job role or position applied for.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Job role is required")
    private String jobRole;

    /**
     * The geographical location of the company or position
     * (e.g. city, state, country, or remote).
     */
    private String companyLocation;

    /**
     * The type of employment (e.g. Full-time, Part-time, Contract,
     * Freelance, Internship).
     */
    private String jobType;

    /**
     * The salary or compensation range associated with the position,
     * if known.
     */
    private String salary;

    /**
     * The date on which the application was submitted.
     */
    private LocalDate applicationDate;

    /**
     * The current status of this job application within the hiring pipeline.
     * <p>
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Application status is required")
    private ApplicationStatus status;

    /**
     * The URL to the job posting or application page.
     */
    private String jobUrl;

    /**
     * Free-text notes about the application, such as preparation notes,
     * follow-up reminders, or feedback received.
     */
    private String notes;

    /**
     * The ID of the resume used when submitting this job application.
     * <p>
     * May be {@code null} if no specific resume was associated.
     * </p>
     */
    private Long resumeId;

}
