package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A single point of the interview score trend.
 *
 * @param completedAt the date and time the interview was completed
 * @param score       the overall score achieved
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreTrendPoint {

    /**
     * The date and time the interview was completed.
     */
    private LocalDateTime completedAt;

    /**
     * The overall score achieved (0–100).
     */
    private Integer score;

}
