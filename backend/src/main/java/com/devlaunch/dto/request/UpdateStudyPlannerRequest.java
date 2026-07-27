package com.devlaunch.dto.request;

import com.devlaunch.entity.enums.StudyPriority;
import com.devlaunch.entity.enums.StudyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for updating an existing study planner entry.
 * <p>
 * Contains the updated study session details that the authenticated
 * user wishes to apply to an existing study planner record.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStudyPlannerRequest {

    /**
     * The updated title or name of the study session or task.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Title is required")
    private String title;

    /**
     * The updated description of the study session or task, including any
     * specific topics, resources, or objectives to cover.
     */
    private String description;

    /**
     * The updated scheduled date for this study session.
     * <p>
     * Specifies the calendar date on which the study activity is planned or
     * was completed. Must not be null.
     * </p>
     */
    @NotNull(message = "Study date is required")
    private LocalDate studyDate;

    /**
     * The updated intended or actual start time of the study session.
     * <p>
     * May be {@code null} if only the date is specified without a specific
     * start time.
     * </p>
     */
    private LocalTime startTime;

    /**
     * The updated intended or actual end time of the study session.
     * <p>
     * May be {@code null} if the session duration is not predefined.
     * </p>
     */
    private LocalTime endTime;

    /**
     * The updated priority level of this study session.
     * <p>
     * Indicates the relative importance or urgency of the study activity.
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Priority is required")
    private StudyPriority priority;

    /**
     * The updated completion status of this study session.
     * <p>
     * Tracks whether the session is pending, in progress, or completed.
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Status is required")
    private StudyStatus status;

}
