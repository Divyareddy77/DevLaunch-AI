package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Response DTO for work experience information.
 * <p>
 * Exposes experience data including the company name, job title,
 * employment type, location, employment dates, and a description.
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
public class ExperienceResponse {

    private Long id;

    private String companyName;

    private String jobTitle;

    private String employmentType;

    private String location;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean currentlyWorking;

    private String description;

}
