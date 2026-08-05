package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.StudyPriority;
import com.devlaunch.entity.enums.StudyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for a study plan entry as seen by an administrator.
 * <p>
 * Shows the study task together with the owning user's identity so admins
 * can monitor learning activity across the platform.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStudyPlannerResponse {

    private Long id;

    private String title;

    private String description;

    private LocalDate studyDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private StudyPriority priority;

    private StudyStatus status;

    private Long userId;

    private String userEmail;

    private String userName;

    private LocalDateTime createdAt;

}
