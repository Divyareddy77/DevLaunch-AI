package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Response DTO for achievement information.
 * <p>
 * Exposes achievement data including the title, description, and the date
 * it was achieved. Internal fields such as the resume association and
 * timestamps are excluded from the response.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementResponse {

    private Long id;

    private String title;

    private String description;

    private LocalDate dateAchieved;

}
