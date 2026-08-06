package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for the evaluation of a single project entry produced
 * by an AI resume review.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectAnalysisResponse {

    /**
     * The name of the project.
     */
    private String projectName;

    /**
     * A qualitative rating of the description quality
     * (e.g. "Detailed", "Good", "Brief", "No description").
     */
    private String descriptionQuality;

    /**
     * The technologies called out in the project.
     */
    private List<String> technologiesMentioned;

    /**
     * Whether the project describes its business impact.
     */
    private Boolean businessImpact;

    /**
     * A qualitative rating of the technical depth (High/Moderate/Low).
     */
    private String technicalDepth;

    /**
     * The action verbs used in the project description.
     */
    private List<String> actionVerbs;

    /**
     * Whether measurable outcomes are present in the description.
     */
    private Boolean measurableOutcomes;

    /**
     * Project-specific improvement suggestions.
     */
    private List<String> suggestions;

}
