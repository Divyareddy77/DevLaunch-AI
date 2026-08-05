package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating or updating an announcement.
 * <p>
 * Carries the title, content, and publish state of the announcement. The
 * author is resolved server-side from the authenticated administrator.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementRequest {

    /**
     * The headline of the announcement.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    /**
     * The full body of the announcement.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Content is required")
    private String content;

    /**
     * Whether the announcement is currently visible.
     * <p>
     * Defaults to {@code true} when not provided.
     * </p>
     */
    private Boolean isActive;

}
