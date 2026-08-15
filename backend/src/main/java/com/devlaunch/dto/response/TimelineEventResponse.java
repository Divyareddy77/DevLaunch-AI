package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.TimelineEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for a single milestone on a job application timeline.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineEventResponse {

    private Long id;

    private TimelineEventType eventType;

    private String title;

    private String notes;

    private LocalDateTime occurredAt;

}
