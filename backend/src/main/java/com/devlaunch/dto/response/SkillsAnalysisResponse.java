package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for the evaluation of the skills section produced by
 * an AI resume review.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillsAnalysisResponse {

    /**
     * The skills matching known technical keywords.
     */
    private List<String> technicalSkills;

    /**
     * The remaining (non-technical) skills.
     */
    private List<String> softSkills;

    /**
     * A note on how the skills are organised on the resume.
     */
    private String organization;

    /**
     * In-demand skills missing from the resume.
     */
    private List<String> missingRelevantSkills;

}
