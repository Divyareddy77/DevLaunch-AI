package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for the evaluation of the professional summary section
 * produced by an AI resume review.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryAnalysisResponse {

    /**
     * The summary quality score, ranging from 0 to 100.
     */
    private Integer score;

    /**
     * The strengths of the current professional summary.
     */
    private List<String> strengths;

    /**
     * How the professional summary could be improved.
     */
    private List<String> suggestions;

    /**
     * An AI-generated improved version of the professional summary
     * that the user can copy and use directly.
     */
    private String improvedSummary;

}
