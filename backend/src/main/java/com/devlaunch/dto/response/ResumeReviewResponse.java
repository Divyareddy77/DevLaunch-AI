package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for an AI-powered resume review.
 * <p>
 * Contains an overall resume quality score (0–100), an Applicant
 * Tracking System (ATS) compatibility score (0–100), the strengths
 * and weaknesses identified in the resume, in-demand skills that are
 * missing, and actionable improvement suggestions.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeReviewResponse {

    /**
     * The ID of the resume that was reviewed.
     */
    private Long resumeId;

    /**
     * The headline/title of the reviewed resume.
     */
    private String resumeTitle;

    /**
     * The overall resume quality score, ranging from 0 to 100.
     */
    private Integer resumeScore;

    /**
     * The Applicant Tracking System compatibility score, ranging from 0 to 100.
     */
    private Integer atsScore;

    /**
     * The strengths identified in the resume.
     */
    private List<String> strengths;

    /**
     * The weaknesses or areas for improvement identified in the resume.
     */
    private List<String> weaknesses;

    /**
     * In-demand skills that are missing from the resume.
     */
    private List<String> missingSkills;

    /**
     * Actionable improvement suggestions, each tied to a resume section.
     */
    private List<ResumeReviewSuggestion> suggestions;

}
