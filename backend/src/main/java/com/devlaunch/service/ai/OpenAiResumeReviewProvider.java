package com.devlaunch.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI-compatible resume review provider.
 * <p>
 * Uses the shared {@link OpenAiChatCompletions} client to call a Chat
 * Completions REST API (OpenAI by default, or any OpenAI-compatible
 * provider) and instructs the model to return a strict JSON analysis of
 * the resume. The provider is only considered configured when an API key
 * is present; otherwise the service layer falls back to the deterministic
 * {@link SampleResumeReviewProvider}. Any network, parsing, or provider
 * error is surfaced so the caller can fall back gracefully.
 * </p>
 *
 * @author DevLaunch
 */
@Service
@Qualifier("openAiResumeReviewProvider")
public class OpenAiResumeReviewProvider implements ResumeReviewProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiResumeReviewProvider.class);

    /** System prompt instructing the model to return strict JSON. */
    private static final String SYSTEM_PROMPT =
            "You are an expert resume reviewer and Applicant Tracking System (ATS) specialist. "
                    + "Analyse the resume provided by the user and return STRICT JSON only, with no "
                    + "markdown formatting and no commentary outside the JSON object. Use exactly this "
                    + "schema: {\"resumeScore\": 0-100 integer, \"atsScore\": 0-100 integer, "
                    + "\"categoryScores\": [{\"category\": string, \"score\": integer, "
                    + "\"maxScore\": integer}], \"strengths\": [string], \"weaknesses\": [string], "
                    + "\"missingSkills\": [string], \"missingSections\": [string], "
                    + "\"foundKeywords\": [string], \"missingKeywords\": [string], "
                    + "\"keywordSuggestions\": [string], \"formattingAnalysis\": [string], "
                    + "\"suggestions\": [{\"section\": string, \"suggestion\": string, "
                    + "\"priority\": \"high\"|\"medium\"|\"low\"}], "
                    + "\"summaryAnalysis\": {\"score\": 0-100 integer, \"strengths\": [string], "
                    + "\"suggestions\": [string], \"improvedSummary\": string}, "
                    + "\"projectAnalyses\": [{\"projectName\": string, \"descriptionQuality\": string, "
                    + "\"technologiesMentioned\": [string], \"businessImpact\": boolean, "
                    + "\"technicalDepth\": string, \"actionVerbs\": [string], "
                    + "\"measurableOutcomes\": boolean, \"suggestions\": [string]}], "
                    + "\"skillsAnalysis\": {\"technicalSkills\": [string], \"softSkills\": [string], "
                    + "\"organization\": string, \"missingRelevantSkills\": [string]}, "
                    + "\"experienceAnalysis\": {\"actionVerbs\": [string], \"responsibilities\": string, "
                    + "\"achievements\": string, \"quantifiedImpact\": boolean, \"suggestions\": [string]}}. "
                    + "The atsScore must be 0-100 and must equal the sum of the categoryScores scores, "
                    + "with maxScore values of 10 (Contact Information), 10 (Professional Summary), "
                    + "20 (Skills), 15 (Projects), 20 (Experience), 5 (Education), 5 (Certifications), "
                    + "5 (Formatting), and 10 (Keyword Relevance). The resumeScore rates overall resume "
                    + "quality and the atsScore rates keyword and structure compatibility with automated "
                    + "screening. Report only missing sections and missing keywords that are genuinely "
                    + "absent. Be specific and constructive.";

    private final OpenAiChatCompletions chatCompletions;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the provider with the shared Chat Completions client.
     *
     * @param chatCompletions the shared OpenAI-compatible chat client
     * @param objectMapper    the Jackson object mapper for response handling
     */
    public OpenAiResumeReviewProvider(final OpenAiChatCompletions chatCompletions,
                                      final ObjectMapper objectMapper) {
        this.chatCompletions = chatCompletions;
        this.objectMapper = objectMapper;
    }

    /**
     * The provider is configured only when the shared chat client has an
     * API key.
     *
     * @return {@code true} when an API key is present, {@code false} otherwise
     */
    @Override
    public boolean isConfigured() {
        return chatCompletions.isConfigured();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends the resume text to the Chat Completions API, parses the
     * model's strict JSON response, and maps it to a
     * {@link ResumeReviewAnalysis}. Throws if the provider is not
     * configured, the API returns an error, or the response cannot be
     * parsed, so the caller can fall back to deterministic analysis.
     * </p>
     */
    @Override
    public ResumeReviewAnalysis analyze(final ResumeContent content, final String targetRole) {
        if (!isConfigured()) {
            throw new IllegalStateException("OpenAI resume review provider is not configured");
        }

        final String userPrompt = buildUserPrompt(content, targetRole);
        final String modelContent = chatCompletions.chat(SYSTEM_PROMPT, userPrompt, 0.3);
        final ResumeReviewAnalysis analysis = parseAnalysis(modelContent);

        log.info("AI resume review completed: resumeScore={}, atsScore={}",
                analysis.resumeScore(), analysis.atsScore());
        return analysis;
    }

    /**
     * Parses the model's strict JSON content into a
     * {@link ResumeReviewAnalysis}, applying defensive fallbacks for
     * missing or malformed fields.
     *
     * @param modelContent the JSON content returned by the model
     * @return the parsed analysis
     * @throws IllegalStateException if the content cannot be parsed
     */
    private ResumeReviewAnalysis parseAnalysis(final String modelContent) {
        try {
            final JsonNode analysis = objectMapper.readTree(modelContent);

            final List<ResumeReviewAnalysis.Suggestion> suggestions = new ArrayList<>();
            final JsonNode suggestionsNode = analysis.path("suggestions");
            if (suggestionsNode.isArray()) {
                for (final JsonNode suggestion : suggestionsNode) {
                    suggestions.add(new ResumeReviewAnalysis.Suggestion(
                            suggestion.path("section").asText("Resume"),
                            suggestion.path("suggestion").asText(""),
                            normalizePriority(suggestion.path("priority").asText("medium"))));
                }
            }

            final List<ResumeReviewAnalysis.CategoryScore> categoryScores = new ArrayList<>();
            final JsonNode categoryNode = analysis.path("categoryScores");
            if (categoryNode.isArray()) {
                for (final JsonNode entry : categoryNode) {
                    categoryScores.add(new ResumeReviewAnalysis.CategoryScore(
                            entry.path("category").asText("Category"),
                            clampScore(entry.path("score").asInt(0)),
                            Math.max(1, entry.path("maxScore").asInt(1))));
                }
            }

            // Enforce the report invariant: the overall ATS score equals the
            // sum of the category scores whenever categories are present, so
            // the ring and the breakdown bars always agree.
            final int atsScore = categoryScores.isEmpty()
                    ? clampScore(analysis.path("atsScore").asInt(0))
                    : clampScore(categoryScores.stream()
                            .mapToInt(ResumeReviewAnalysis.CategoryScore::score)
                            .sum());

            return new ResumeReviewAnalysis(
                    clampScore(analysis.path("resumeScore").asInt(0)),
                    atsScore,
                    readStringArray(analysis.path("strengths")),
                    readStringArray(analysis.path("weaknesses")),
                    readStringArray(analysis.path("missingSkills")),
                    List.copyOf(suggestions),
                    List.copyOf(categoryScores),
                    readStringArray(analysis.path("missingSections")),
                    readStringArray(analysis.path("foundKeywords")),
                    readStringArray(analysis.path("missingKeywords")),
                    readStringArray(analysis.path("keywordSuggestions")),
                    readStringArray(analysis.path("formattingAnalysis")),
                    parseSummaryAnalysis(analysis.path("summaryAnalysis")),
                    parseProjectAnalyses(analysis.path("projectAnalyses")),
                    parseSkillsAnalysis(analysis.path("skillsAnalysis")),
                    parseExperienceAnalysis(analysis.path("experienceAnalysis")));
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse AI provider response", e);
        }
    }

    /**
     * Parses the summary analysis object with defensive defaults.
     */
    private ResumeReviewAnalysis.SummaryAnalysis parseSummaryAnalysis(final JsonNode node) {
        if (!node.isObject()) {
            return new ResumeReviewAnalysis.SummaryAnalysis(
                    0, List.of(), List.of(), "");
        }
        return new ResumeReviewAnalysis.SummaryAnalysis(
                clampScore(node.path("score").asInt(0)),
                readStringArray(node.path("strengths")),
                readStringArray(node.path("suggestions")),
                node.path("improvedSummary").asText(""));
    }

    /**
     * Parses the per-project analyses with defensive defaults.
     */
    private List<ResumeReviewAnalysis.ProjectAnalysis> parseProjectAnalyses(final JsonNode node) {
        final List<ResumeReviewAnalysis.ProjectAnalysis> analyses = new ArrayList<>();
        if (node.isArray()) {
            for (final JsonNode entry : node) {
                analyses.add(new ResumeReviewAnalysis.ProjectAnalysis(
                        entry.path("projectName").asText("Project"),
                        entry.path("descriptionQuality").asText(""),
                        readStringArray(entry.path("technologiesMentioned")),
                        entry.path("businessImpact").asBoolean(false),
                        entry.path("technicalDepth").asText(""),
                        readStringArray(entry.path("actionVerbs")),
                        entry.path("measurableOutcomes").asBoolean(false),
                        readStringArray(entry.path("suggestions"))));
            }
        }
        return List.copyOf(analyses);
    }

    /**
     * Parses the skills analysis object with defensive defaults.
     */
    private ResumeReviewAnalysis.SkillsAnalysis parseSkillsAnalysis(final JsonNode node) {
        if (!node.isObject()) {
            return new ResumeReviewAnalysis.SkillsAnalysis(List.of(), List.of(), "", List.of());
        }
        return new ResumeReviewAnalysis.SkillsAnalysis(
                readStringArray(node.path("technicalSkills")),
                readStringArray(node.path("softSkills")),
                node.path("organization").asText(""),
                readStringArray(node.path("missingRelevantSkills")));
    }

    /**
     * Parses the experience analysis object with defensive defaults.
     */
    private ResumeReviewAnalysis.ExperienceAnalysis parseExperienceAnalysis(final JsonNode node) {
        if (!node.isObject()) {
            return new ResumeReviewAnalysis.ExperienceAnalysis(
                    List.of(), "", "", false, List.of());
        }
        return new ResumeReviewAnalysis.ExperienceAnalysis(
                readStringArray(node.path("actionVerbs")),
                node.path("responsibilities").asText(""),
                node.path("achievements").asText(""),
                node.path("quantifiedImpact").asBoolean(false),
                readStringArray(node.path("suggestions")));
    }

    /**
     * Reads a JSON array of strings, ignoring blank entries.
     *
     * @param node the JSON array node
     * @return the list of non-blank strings
     */
    private List<String> readStringArray(final JsonNode node) {
        final List<String> values = new ArrayList<>();
        if (node.isArray()) {
            for (final JsonNode element : node) {
                final String value = element.asText("");
                if (!value.isBlank()) {
                    values.add(value.trim());
                }
            }
        }
        return List.copyOf(values);
    }

    /**
     * Clamps a score into the valid 0–100 range.
     */
    private int clampScore(final int score) {
        return Math.max(0, Math.min(100, score));
    }

    /**
     * Normalises a provider-supplied priority to high/medium/low.
     */
    private String normalizePriority(final String priority) {
        final String normalized = priority == null ? "" : priority.trim().toLowerCase();
        if ("high".equals(normalized)) {
            return "high";
        }
        if ("low".equals(normalized)) {
            return "low";
        }
        return "medium";
    }

    /**
     * Builds the user prompt containing the resume text and optional
     * target role.
     */
    private String buildUserPrompt(final ResumeContent content, final String targetRole) {
        final StringBuilder prompt = new StringBuilder();
        prompt.append("Please review the following resume");
        if (targetRole != null && !targetRole.isBlank()) {
            prompt.append(" for a ").append(targetRole.trim()).append(" role");
        }
        prompt.append(":\n\n").append(content.toPlainText());
        return prompt.toString();
    }

}
