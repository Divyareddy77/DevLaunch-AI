package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.StudyPriority;
import com.devlaunch.entity.enums.StudyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response DTO for study planner information.
 * <p>
 * Exposes study session data including the title, description, date,
 * time range, priority, and status. Internal fields such as the user
 * association and timestamps are excluded from the response.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlannerResponse {

    private Long id;

    private String title;

    private String description;

    private LocalDate studyDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private StudyPriority priority;

    private StudyStatus status;

}
