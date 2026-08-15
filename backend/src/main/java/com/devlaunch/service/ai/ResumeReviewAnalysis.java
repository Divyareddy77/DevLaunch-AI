package com.devlaunch.service.ai;

import java.util.List;

/**
 * Internal analysis result produced by a {@link ResumeReviewProvider}.
 * <p>
 * This record is the provider-agnostic outcome of a resume review and
 * is translated into the public {@code ResumeReviewResponse} DTO by the
 * AI service layer, which attaches the resume identifiers.
 * </p>
 * <p>
 * Beyond the two headline scores, the analysis carries a professional
 * ATS report: weighted category scores that always sum to {@link #atsScore()}
 * (which in turn sums to 100), detected missing sections, keyword analysis,
 * a formatting review, an evaluation of the professional summary (including
 * an AI-generated improved version), and per-project, per-skill, and
 * per-experience analyses.
 * </p>
 *
 * @param resumeScore         the overall resume quality score (0–100)
 * @param atsScore            the overall Applicant Tracking System score
 *                            (0–100), equal to the sum of {@code categoryScores}
 * @param strengths           the strengths identified in the resume
 * @param weaknesses          the weaknesses or areas for improvement
 * @param missingSkills       in-demand skills missing from the resume
 * @param suggestions         actionable improvement suggestions
 * @param categoryScores      the weighted category breakdown of the ATS score
 * @param missingSections     standard resume sections that are genuinely absent
 * @param foundKeywords       technical keywords detected in the resume text
 * @param missingKeywords     common in-demand keywords that are absent
 * @param keywordSuggestions  suggestions for improving keyword coverage
 * @param formattingAnalysis  findings on section order, headings, contact
 *                            completeness, length, and readability
 * @param summaryAnalysis     evaluation of the professional summary, including
 *                            an improved version
 * @param projectAnalyses     per-project quality evaluations
 * @param skillsAnalysis      evaluation of the skills section
 * @param experienceAnalysis  evaluation of the experience section
 * @author DevLaunch
 */
public record ResumeReviewAnalysis(
        int resumeScore,
        int atsScore,
        List<String> strengths,
        List<String> weaknesses,
        List<String> missingSkills,
        List<Suggestion> suggestions,
        List<CategoryScore> categoryScores,
        List<String> missingSections,
        List<String> foundKeywords,
        List<String> missingKeywords,
        List<String> keywordSuggestions,
        List<String> formattingAnalysis,
        SummaryAnalysis summaryAnalysis,
        List<ProjectAnalysis> projectAnalyses,
        SkillsAnalysis skillsAnalysis,
        ExperienceAnalysis experienceAnalysis) {

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

    /**
     * A weighted category contributing to the overall ATS score.
     *
     * @param category the category name (e.g. "Skills", "Experience")
     * @param score    the points achieved in this category
     * @param maxScore the maximum points available in this category
     */
    public record CategoryScore(String category, int score, int maxScore) {
    }

    /**
     * Evaluation of the professional summary section.
     *
     * @param score           the summary quality score (0–100)
     * @param strengths       the strengths of the current summary
     * @param suggestions     how the summary could be improved
     * @param improvedSummary an AI-generated improved version of the summary
     */
    public record SummaryAnalysis(
            int score,
            List<String> strengths,
            List<String> suggestions,
            String improvedSummary) {
    }

    /**
     * Evaluation of a single project entry.
     *
     * @param projectName           the name of the project
     * @param descriptionQuality    a qualitative rating of the description
     *                              (e.g. "Detailed", "Brief")
     * @param technologiesMentioned the technologies called out in the project
     * @param businessImpact        whether business impact is described
     * @param technicalDepth        a qualitative rating of technical depth
     * @param actionVerbs           the action verbs used in the description
     * @param measurableOutcomes    whether measurable outcomes are present
     * @param suggestions           project-specific improvement suggestions
     */
    public record ProjectAnalysis(
            String projectName,
            String descriptionQuality,
            List<String> technologiesMentioned,
            boolean businessImpact,
            String technicalDepth,
            List<String> actionVerbs,
            boolean measurableOutcomes,
            List<String> suggestions) {
    }

    /**
     * Evaluation of the skills section.
     *
     * @param technicalSkills      the skills matching known technical keywords
     * @param softSkills           the remaining (non-technical) skills
     * @param organization         a note on how the skills are organised
     * @param missingRelevantSkills in-demand skills missing from the resume
     */
    public record SkillsAnalysis(
            List<String> technicalSkills,
            List<String> softSkills,
            String organization,
            List<String> missingRelevantSkills) {
    }

    /**
     * Evaluation of the experience section.
     *
     * @param actionVerbs        the action verbs found across the roles
     * @param responsibilities   a note on how responsibilities are described
     * @param achievements       a note on how achievements are highlighted
     * @param quantifiedImpact   whether quantified impact is present
     * @param suggestions        experience-specific improvement suggestions
     */
    public record ExperienceAnalysis(
            List<String> actionVerbs,
            String responsibilities,
            String achievements,
            boolean quantifiedImpact,
            List<String> suggestions) {
    }

}
