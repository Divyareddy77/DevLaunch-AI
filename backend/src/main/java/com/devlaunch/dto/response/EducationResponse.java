package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Response DTO for education information.
 * <p>
 * Exposes education data including the institution name, degree,
 * field of study, grade, dates of attendance, and a description.
 * Internal fields such as the resume association and timestamps
 * are excluded from the response.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationResponse {

    private Long id;

    private String institutionName;

    private String degree;

    private String fieldOfStudy;

    private String grade;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean currentlyStudying;

    private String description;

}
