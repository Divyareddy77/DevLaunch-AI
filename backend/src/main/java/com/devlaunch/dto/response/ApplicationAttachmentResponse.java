package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.AttachmentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for a document attached to a job application.
 * <p>
 * Exposes only the display-safe metadata (original file name, content
 * type, size, category, upload time). The opaque stored file name is
 * never returned to the client.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationAttachmentResponse {

    private Long id;

    private String fileName;

    private String contentType;

    private Long fileSize;

    private AttachmentCategory category;

    private LocalDateTime createdAt;

}
