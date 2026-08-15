package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for an announcement as seen by an administrator.
 * <p>
 * Includes the author's identity and the publish state so admins can
 * manage the announcement list.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAnnouncementResponse {

    private Long id;

    private String title;

    private String content;

    private Boolean isActive;

    private Long createdById;

    private String createdByEmail;

    private String createdByName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
