package com.devlaunch.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Shared OpenAI-compatible Chat Completions client used by the AI module.
 * <p>
 * Encapsulates the HTTP transport, authentication, and payload construction
 * for calling a Chat Completions REST API (OpenAI by default, or any
 * OpenAI-compatible provider). Both the resume review and mock interview
 * providers reuse this client so the LLM integration lives in exactly one
 * place. The client is only considered configured when an API key is
 * present; otherwise the service layer falls back to the deterministic
 * providers.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class OpenAiChatCompletions {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    /**
     * Constructs the Chat Completions client with the configured LLM settings.
     *
     * @param objectMapper          the Jackson object mapper for payload building
     * @param apiKey                the LLM provider API key (blank when not configured)
     * @param model                 the LLM model identifier
     * @param baseUrl               the OpenAI-compatible API base URL
     * @param connectTimeoutSeconds the connection timeout in seconds
     * @param readTimeoutSeconds    the read timeout in seconds
     */
    public OpenAiChatCompletions(final ObjectMapper objectMapper,
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
     * The client is configured only when an API key has been supplied.
     *
     * @return {@code true} when an API key is present, {@code false} otherwise
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Sends a Chat Completions request and returns the model's message
     * content.
     * <p>
     * The response format is set to {@code json_object} so the model is
     * strongly encouraged to return strict JSON, which the callers then
     * parse. Throws {@link IllegalStateException} when the provider
     * returns an error, an empty response, or no usable content, so the
     * service layer can fall back to deterministic analysis.
     * </p>
     *
     * @param systemPrompt the system message instructing the model
     * @param userPrompt   the user message containing the content to process
     * @param temperature  the sampling temperature (0–2)
     * @return the model's message content
     */
    public String chat(final String systemPrompt, final String userPrompt, final double temperature) {
        final ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", model);
        payload.put("temperature", temperature);

        final ArrayNode messages = payload.putArray("messages");
        final ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);

        final ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);

        final ObjectNode responseFormat = payload.putObject("response_format");
        responseFormat.put("type", "json_object");

        final String responseBody = restClient.post()
                .uri(baseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);

        return extractContent(responseBody);
    }

    /**
     * Extracts the model's message content from a Chat Completions
     * response body, surfacing provider errors when present.
     *
     * @param responseBody the raw JSON response body
     * @return the model's message content
     * @throws IllegalStateException if the response cannot be parsed or
     *                               contains no usable content
     */
    private String extractContent(final String responseBody) {
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
                throw new IllegalStateException("AI provider returned no content");
            }

            return content;
        } catch (final IllegalStateException e) {
            throw e;
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to parse AI provider response", e);
        }
    }

}
