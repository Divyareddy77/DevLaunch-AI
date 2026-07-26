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
 * Request DTO for updating an existing experience record.
 * <p>
 * Contains the updated professional details that the authenticated
 * user wishes to apply to an existing work experience entry on their resume.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateExperienceRequest {

    /**
     * The updated name of the company or organisation.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Company name is required")
    private String companyName;

    /**
     * The updated job title or position held.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Job title is required")
    private String jobTitle;

    /**
     * The updated type of employment.
     */
    private String employmentType;

    /**
     * The updated geographical location of the job.
     */
    private String location;

    /**
     * The updated employment start date.
     * <p>
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    /**
     * The updated employment end date.
     */
    private LocalDate endDate;

    /**
     * Indicates whether the user is currently working in this role.
     */
    private Boolean currentlyWorking;

    /**
     * The updated free-text description of this role.
     */
    private String description;

}
