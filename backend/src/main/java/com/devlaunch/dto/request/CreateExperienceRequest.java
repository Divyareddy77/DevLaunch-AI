package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for creating a new experience record.
 * <p>
 * Contains the professional details required to add a work experience
 * entry to a specific resume owned by the authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateExperienceRequest {

    /**
     * The name of the company or organisation where the user worked.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Company name is required")
    private String companyName;

    /**
     * The job title or position held by the user at the company.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Job title is required")
    private String jobTitle;

    /**
     * The type of employment (e.g. Full-time, Part-time, Contract,
     * Freelance, Internship).
     */
    private String employmentType;

    /**
     * The geographical location where the job was performed.
     */
    private String location;

    /**
     * The date on which the employment started.
     * <p>
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    /**
     * The date on which the employment ended.
     */
    private LocalDate endDate;

    /**
     * Indicates whether the user is currently working in this role.
     */
    private Boolean currentlyWorking;

    /**
     * A free-text description of the responsibilities and achievements
     * in this role.
     */
    private String description;

}
