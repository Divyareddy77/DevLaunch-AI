package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for the evaluation of the experience section produced
 * by an AI resume review.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceAnalysisResponse {

    /**
     * The action verbs found across the experience entries.
     */
    private List<String> actionVerbs;

    /**
     * A note on how responsibilities are described.
     */
    private String responsibilities;

    /**
     * A note on how achievements are highlighted.
     */
    private String achievements;

    /**
     * Whether quantified impact is present in the experience section.
     */
    private Boolean quantifiedImpact;

    /**
     * Experience-specific improvement suggestions.
     */
    private List<String> suggestions;

}
