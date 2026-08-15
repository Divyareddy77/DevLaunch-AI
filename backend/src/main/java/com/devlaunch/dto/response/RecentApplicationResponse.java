package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Response DTO for a recent job application shown on the dashboard widget.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentApplicationResponse {

    private Long id;

    private String companyName;

    private String jobRole;

    private ApplicationStatus status;

    private LocalDate applicationDate;

}
