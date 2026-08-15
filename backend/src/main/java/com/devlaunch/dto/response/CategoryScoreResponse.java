package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for a single weighted category contributing to the
 * overall ATS score of a resume review.
 * <p>
 * The category scores of a review always sum to the overall ATS score
 * (which in turn sums to 100).
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryScoreResponse {

    /**
     * The category name (e.g. "Skills", "Experience", "Formatting").
     */
    private String category;

    /**
     * The points achieved in this category.
     */
    private Integer score;

    /**
     * The maximum points available in this category.
     */
    private Integer maxScore;

}
