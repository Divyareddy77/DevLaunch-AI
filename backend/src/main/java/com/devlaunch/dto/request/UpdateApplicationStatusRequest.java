package com.devlaunch.dto.request;

import com.devlaunch.entity.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating only the status of a job application.
 * <p>
 * Used by the Kanban board drag-and-drop so moving a card to another
 * column only changes the status — no other field is touched. The service
 * automatically records a timeline event and a notification for the
 * change.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateApplicationStatusRequest {

    /**
     * The new status for the application.
     * <p>
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Application status is required")
    private ApplicationStatus status;

}
