package com.devlaunch.service.ai;

import java.util.List;

/**
 * Internal analysis result produced by a {@link ResumeReviewProvider}.
 * <p>
 * This record is the provider-agnostic outcome of a resume review and
 * is translated into the public {@code ResumeReviewResponse} DTO by the
 * AI service layer, which attaches the resume identifiers.
 * </p>
 *
 * @param resumeScore    the overall resume quality score (0–100)
 * @param atsScore       the Applicant Tracking System score (0–100)
 * @param strengths      the strengths identified in the resume
 * @param weaknesses     the weaknesses or areas for improvement
 * @param missingSkills  in-demand skills missing from the resume
 * @param suggestions    actionable improvement suggestions
 * @author DevLaunch
 */
public record ResumeReviewAnalysis(
        int resumeScore,
        int atsScore,
        List<String> strengths,
        List<String> weaknesses,
        List<String> missingSkills,
        List<Suggestion> suggestions) {

    /**
     * A single improvement suggestion tied to a resume section.
     *
     * @param section    the resume section the suggestion relates to
     * @param suggestion the human-readable suggestion text
     * @param priority   the priority level: {@code high}, {@code medium},
     *                   or {@code low}
     */
    public record Suggestion(String section, String suggestion, String priority) {
    }

}
