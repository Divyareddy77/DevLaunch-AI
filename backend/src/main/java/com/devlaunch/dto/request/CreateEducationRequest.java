package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for creating a new education record.
 * <p>
 * Contains the academic details required to add an education entry
 * to a specific resume owned by the authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEducationRequest {

    /**
     * The name of the educational institution.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Institution name is required")
    private String institutionName;

    /**
     * The degree or certification obtained.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Degree is required")
    private String degree;

    /**
     * The field or discipline of study.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Field of study is required")
    private String fieldOfStudy;

    /**
     * The grade or GPA achieved for this qualification.
     */
    private String grade;

    /**
     * The date on which the educational programme started.
     */
    private LocalDate startDate;

    /**
     * The date on which the educational programme ended
     * (or is expected to end).
     */
    private LocalDate endDate;

    /**
     * Indicates whether the user is currently studying at this institution.
     */
    private Boolean currentlyStudying;

    /**
     * A free-text description of this educational experience.
     */
    private String description;

}
