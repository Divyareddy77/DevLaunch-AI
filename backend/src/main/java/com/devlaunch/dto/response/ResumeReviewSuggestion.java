package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for a single improvement suggestion produced by an
 * AI resume review.
 * <p>
 * Each suggestion targets a specific section of the resume and
 * carries a priority level indicating how impactful addressing
 * it would be.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeReviewSuggestion {

    /**
     * The resume section the suggestion relates to
     * (e.g. "Summary", "Experience", "Skills", "Keywords").
     */
    private String section;

    /**
     * The human-readable suggestion text.
     */
    private String suggestion;

    /**
     * The priority of the suggestion: {@code high}, {@code medium},
     * or {@code low}.
     */
    private String priority;

}
