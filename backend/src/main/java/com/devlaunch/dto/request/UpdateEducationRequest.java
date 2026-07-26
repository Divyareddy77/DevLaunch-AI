package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for updating an existing education record.
 * <p>
 * Contains the updated academic details that the authenticated
 * user wishes to apply to an existing education entry on their resume.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEducationRequest {

    /**
     * The updated name of the educational institution.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Institution name is required")
    private String institutionName;

    /**
     * The updated degree or certification obtained.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Degree is required")
    private String degree;

    /**
     * The updated field or discipline of study.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Field of study is required")
    private String fieldOfStudy;

    /**
     * The updated grade or GPA achieved for this qualification.
     */
    private String grade;

    /**
     * The updated start date of the educational programme.
     */
    private LocalDate startDate;

    /**
     * The updated end date of the educational programme
     * (or expected end date).
     */
    private LocalDate endDate;

    /**
     * Indicates whether the user is currently studying at this institution.
     */
    private Boolean currentlyStudying;

    /**
     * The updated free-text description of this educational experience.
     */
    private String description;

}
