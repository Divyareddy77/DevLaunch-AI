package com.devlaunch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for adding a private interview note entry to a job
 * application.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInterviewNoteRequest {

    /**
     * The note content (e.g. "Asked Java Streams", "Need to revise
     * multithreading").
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Note content is required")
    private String content;

}
