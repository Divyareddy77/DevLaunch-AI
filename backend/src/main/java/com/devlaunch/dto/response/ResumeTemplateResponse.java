package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for resume template information.
 * <p>
 * Exposes template data including the name, description, and optional
 * preview image URL. System-managed fields such as timestamps are
 * excluded from the response.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeTemplateResponse {

    private Long id;

    private String name;

    private String description;

    private String previewImageUrl;

}
