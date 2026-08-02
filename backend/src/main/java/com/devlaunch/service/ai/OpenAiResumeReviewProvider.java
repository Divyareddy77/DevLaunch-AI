package com.devlaunch.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI-compatible resume review provider.
 * <p>
 * Calls a Chat Completions REST API (OpenAI by default, or any
 * OpenAI-compatible provider) and instructs the model to return a
 * strict JSON analysis of the resume. The provider is only considered
 * configured when an API key is present; otherwise the service layer
 * falls back to the deterministic {@link SampleResumeReviewProvider}.
 * Any network, parsing, or provider error is surfaced so the caller
 * can fall back gracefully.
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
                    + "\"strengths\": [string], \"weaknesses\": [string], \"missingSkills\": [string], "
                    + "\"suggestions\": [{\"section\": string, \"suggestion\": string, "
                    + "\"priority\": \"high\"|\"medium\"|\"low\"}]}. The resumeScore rates overall resume "
                    + "quality and the atsScore rates keyword and structure compatibility with automated "
                    + "screening. Be specific and constructive.";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    /**
     * Constructs the provider with the configured LLM settings.
     *
     * @param objectMapper          the Jackson object mapper for request/response handling
     * @param apiKey                the LLM provider API key (blank when not configured)
     * @param model                 the LLM model identifier
     * @param baseUrl               the OpenAI-compatible API base URL
     * @param connectTimeoutSeconds the connection timeout in seconds
     * @param readTimeoutSeconds    the read timeout in seconds
     */
    public OpenAiResumeReviewProvider(final ObjectMapper objectMapper,
                                      @Value("${devlaunch.ai.provider-api-key:}") final String apiKey,
                                      @Value("${devlaunch.ai.model:gpt-4o-mini}") final String model,
                                      @Value("${devlaunch.ai.base-url:https://api.openai.com/v1}") final String baseUrl,
                                      @Value("${devlaunch.ai.connect-timeout-seconds:10}") final int connectTimeoutSeconds,
                                      @Value("${devlaunch.ai.read-timeout-seconds:60}") final int readTimeoutSeconds) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    /**
     * The provider is configured only when an API key has been supplied.
     *
     * @return {@code true} when an API key is present, {@code false} otherwise
     */
    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
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
        final String responseBody = callChatCompletions(userPrompt);
        final ResumeReviewAnalysis analysis = parseAnalysis(responseBody);

        log.info("AI resume review completed: resumeScore={}, atsScore={}",
                analysis.resumeScore(), analysis.atsScore());
        return analysis;
    }

    /**
     * Performs the Chat Completions HTTP request and returns the raw
     * response body.
     *
     * @param userPrompt the user message containing the resume text
     * @return the raw JSON response body
     */
    private String callChatCompletions(final String userPrompt) {
        final ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", model);
        payload.put("temperature", 0.3);

        final ArrayNode messages = payload.putArray("messages");
        final ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);

        final ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);

        final ObjectNode responseFormat = payload.putObject("response_format");
        responseFormat.put("type", "json_object");

        return restClient.post()
                .uri(baseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);
    }

    /**
     * Parses the Chat Completions response body into a
     * {@link ResumeReviewAnalysis}, extracting the model's JSON payload
     * from the first choice message and applying defensive fallbacks for
     * missing or malformed fields.
     *
     * @param responseBody the raw JSON response body
     * @return the parsed analysis
     * @throws IllegalStateException if the response cannot be parsed or
     *                               contains no usable content
     */
    private ResumeReviewAnalysis parseAnalysis(final String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("AI provider returned an empty response");
        }

        try {
            final JsonNode root = objectMapper.readTree(responseBody);

            final JsonNode errorMessage = root.path("error").path("message");
            if (!errorMessage.isMissingNode() && !errorMessage.asText().isBlank()) {
                throw new IllegalStateException("AI provider error: " + errorMessage.asText());
            }

            final String content = root.at("/choices/0/message/content").asText(null);
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("AI provider returned no analysis content");
            }

            final JsonNode analysis = objectMapper.readTree(content);

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

            return new ResumeReviewAnalysis(
                    clampScore(analysis.path("resumeScore").asInt(0)),
                    clampScore(analysis.path("atsScore").asInt(0)),
                    readStringArray(analysis.path("strengths")),
                    readStringArray(analysis.path("weaknesses")),
                    readStringArray(analysis.path("missingSkills")),
                    List.copyOf(suggestions));
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse AI provider response", e);
        }
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
