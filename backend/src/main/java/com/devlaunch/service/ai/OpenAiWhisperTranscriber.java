package com.devlaunch.service.ai;

import com.devlaunch.exception.AiTranscriptionException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;

/**
 * Reusable OpenAI Whisper speech-to-text client.
 * <p>
 * Shares the same {@code devlaunch.ai} configuration as the rest of the AI
 * module (API key, base URL, timeouts) and sends recorded audio to the
 * Whisper {@code /audio/transcriptions} endpoint, returning only the
 * transcribed text and the audio duration. The API key lives exclusively
 * on the backend — clients always upload through this service and never
 * talk to OpenAI directly. The client is only considered configured when
 * an API key is present.
 * </p>
 * <p>
 * Failures are diagnosed deliberately: the complete provider error
 * response is logged (status + body) so invalid keys, exhausted quotas,
 * wrong endpoints, and rejected formats are visible in the backend logs,
 * and the provider's own message is surfaced to the caller (and the
 * client) instead of a generic "try again" string.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class OpenAiWhisperTranscriber {

    private static final Logger log = LoggerFactory.getLogger(OpenAiWhisperTranscriber.class);

    /** The Whisper model used for transcription. */
    private static final String DEFAULT_WHISPER_MODEL = "whisper-1";

    /** Response format that also reports the audio duration. */
    private static final String VERBOSE_JSON_FORMAT = "verbose_json";

    /** Audio content types accepted by Whisper. */
    private static final Set<String> SUPPORTED_AUDIO_PREFIXES = Set.of(
            "audio/", "video/mp4", "video/webm", "video/ogg", "video/x-m4v", "video/quicktime");

    /** Provider messages longer than this are truncated for the client. */
    private static final int MAX_PROVIDER_MESSAGE_LENGTH = 240;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    /**
     * Constructs the Whisper client with the shared AI module settings.
     *
     * @param objectMapper            the Jackson object mapper for response handling
     * @param apiKey                  the LLM provider API key (blank when not configured)
     * @param baseUrl                 the OpenAI-compatible API base URL
     * @param connectTimeoutSeconds   the connection timeout in seconds
     * @param readTimeoutSeconds      the read timeout in seconds
     * @param whisperModel            the Whisper model identifier
     */
    public OpenAiWhisperTranscriber(final ObjectMapper objectMapper,
                                    @Value("${devlaunch.ai.provider-api-key:}") final String apiKey,
                                    @Value("${devlaunch.ai.base-url:https://api.openai.com/v1}") final String baseUrl,
                                    @Value("${devlaunch.ai.connect-timeout-seconds:10}") final int connectTimeoutSeconds,
                                    @Value("${devlaunch.ai.whisper-read-timeout-seconds:300}") final int readTimeoutSeconds,
                                    @Value("${devlaunch.ai.whisper-model:whisper-1}") final String whisperModel) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = whisperModel == null || whisperModel.isBlank()
                ? DEFAULT_WHISPER_MODEL : whisperModel;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        // The JDK HttpClient factory is used instead of SimpleClientHttp
        // RequestFactory because the latter cannot read error response
        // bodies (the provider's error message comes back empty), which
        // would hide invalid-key, quota, and format errors.
        final HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();
        final JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();

        log.info("OpenAI Whisper transcriber initialized: configured={}, model={}, baseUrl={}",
                isConfigured(), this.model, this.baseUrl);
    }

    /**
     * The transcriber is configured only when an API key has been supplied.
     *
     * @return {@code true} when an API key is present, {@code false} otherwise
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Transcribes the uploaded audio through Whisper.
     * <p>
     * Validates that the file is a non-empty audio payload, forwards it to
     * the Whisper endpoint as multipart form data, and parses the
     * transcribed text together with the audio duration reported by the
     * provider (falling back to the client-reported duration when the
     * provider does not return one). On any provider failure the complete
     * error response is logged and the provider's own message is included
     * in the thrown exception so the cause is visible end-to-end.
     * </p>
     *
     * @param file                  the recorded audio to transcribe
     * @param clientDurationSeconds the recording duration tracked by the
     *                              client, used as a fallback duration
     * @return the transcribed text and audio duration
     * @throws IllegalArgumentException when the file is empty or not an
     *                                  audio payload
     * @throws AiTranscriptionException when the provider is not configured,
     *                                  rejects the audio, or cannot be reached
     */
    public WhisperTranscription transcribe(final MultipartFile file,
                                           final Integer clientDurationSeconds) {
        if (!isConfigured()) {
            throw new AiTranscriptionException(
                    "Voice transcription is unavailable because the AI provider API key "
                            + "(AI_PROVIDER_API_KEY) is not configured on the server. Voice transcription requires an OpenAI API key. " +
                            "You can still answer using text or configure an API key to enable voice transcription.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "No audio was received. Please record your answer and try again.");
        }
        if (!isSupportedAudio(file)) {
            throw new IllegalArgumentException(
                    "The uploaded file is not a supported audio format. Please record again.");
        }

        log.debug("Transcribe request received: file={}, size={} bytes, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        // Build the multipart payload with a plain MultiValueMap: this is
        // written by FormHttpMessageConverter and needs only spring-web
        // classes, unlike MultipartBodyBuilder which pulls in reactive-
        // streams types that are absent without WebFlux on the classpath.
        final MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", file.getResource());
        formData.add("model", model);
        formData.add("response_format", VERBOSE_JSON_FORMAT);

        try {
            final String responseBody = restClient.post()
                    .uri(baseUrl + "/audio/transcriptions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(formData)
                    .retrieve()
                    // A redirect from a misconfigured endpoint is not an
                    // "error" for RestClient, so surface it explicitly
                    // instead of letting the empty redirect body flow into
                    // response parsing.
                    .onStatus(HttpStatusCode::is3xxRedirection, (request, response) ->
                            throwTranscriptionError(response))
                    .body(String.class);

            final double fallbackDuration = clientDurationSeconds == null
                    ? 0.0 : clientDurationSeconds;
            final WhisperTranscription result =
                    parseResponse(responseBody, fallbackDuration);

            log.info("Audio transcribed: duration={}s, transcriptLength={}",
                    Math.round(result.duration()), result.transcript().length());
            return result;
        } catch (final RestClientResponseException e) {
            // The provider rejected the request (invalid key, insufficient
            // credits, unsupported format, wrong endpoint, ...). With the
            // JDK HttpClient factory the full error body is available on
            // the exception, so log it completely and surface the
            // provider's own message to the caller.
            final String rawBody = e.getResponseBodyAsString();
            final String providerMessage = providerMessage(rawBody);
            log.error("Whisper transcription failed: HTTP {}, provider response: {}",
                    e.getStatusCode().value(),
                    rawBody.isBlank() ? "<no response body>" : rawBody, e);
            throw new AiTranscriptionException(
                    "Speech-to-text failed" + (providerMessage.isEmpty()
                            ? ". Please try again." : ": " + providerMessage), e);
        } catch (final RestClientException e) {
            // A timeout (read or connect) is diagnosed from the cause chain,
            // which may surface as SocketTimeoutException or ConnectException
            // wrapped in either ResourceAccessException or a generic
            // RestClientException depending on where the failure occurred.
            if (isTimeout(e)) {
                log.error("Whisper transcription timed out while contacting the provider", e);
                throw new AiTranscriptionException(
                        "Speech-to-text timed out. Please check your connection and try again.", e);
            }
            log.error("Whisper transcription request failed", e);
            throw new AiTranscriptionException(
                    "Speech-to-text is temporarily unavailable. Please try again later.", e);
        }
    }

    /**
     * Handles a rejected provider response (4xx/5xx): logs the complete
     * response and throws a transcription exception carrying the provider's
     * own error message.
     *
     * @param response the error response from the provider
     * @throws IOException if the response body cannot be read
     */
    private void throwTranscriptionError(final ClientHttpResponse response) throws IOException {
        final String rawBody = readResponseBody(response);
        log.error("Whisper transcription failed: HTTP {}, provider response: {}",
                response.getStatusCode().value(),
                rawBody.isBlank() ? "<no response body>" : rawBody);
        final String providerMessage = providerMessage(rawBody);
        throw new AiTranscriptionException(
                "Speech-to-text failed" + (providerMessage.isEmpty()
                        ? ". Please try again." : ": " + providerMessage));
    }

    /**
     * Reads the complete error response body as UTF-8 text.
     *
     * @param response the error response
     * @return the response body, or an empty string
     * @throws IOException if the body cannot be read
     */
    private String readResponseBody(final ClientHttpResponse response) throws IOException {
        final InputStream body = response.getBody();
        if (body == null) {
            return "";
        }
        return new String(body.readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Extracts the provider's error message from a rejected response body.
     * <p>
     * Parses the standard OpenAI error shape {@code {"error":{"message":…}}}
     * and falls back to the raw body when it cannot be parsed. The message
     * is truncated so a very long provider response never floods the client
     * response.
     * </p>
     *
     * @param rawBody the raw response body
     * @return the provider's error message, or an empty string
     */
    private String providerMessage(final String rawBody) {
        if (rawBody.isBlank()) {
            return "";
        }
        try {
            final String message = objectMapper.readTree(rawBody)
                    .path("error").path("message").asText("").trim();
            if (!message.isEmpty()) {
                return truncate(message);
            }
        } catch (final Exception parseFailure) {
            log.debug("Could not parse provider error body as JSON: {}", parseFailure.getMessage());
        }
        return truncate(rawBody);
    }

    /**
     * Whether the exception's cause chain contains a socket timeout or
     * connection failure.
     *
     * @param cause the root exception
     * @return {@code true} when a timeout/connect failure is present
     */
    private boolean isTimeout(final Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current instanceof ConnectException
                    || current instanceof HttpTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Truncates a provider message so it stays readable in the client UI.
     *
     * @param message the raw message
     * @return the truncated message
     */
    private String truncate(final String message) {
        if (message.length() <= MAX_PROVIDER_MESSAGE_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_PROVIDER_MESSAGE_LENGTH) + "…";
    }

    /**
     * Determines whether the uploaded file looks like a Whisper-supported
     * audio or video payload.
     *
     * @param file the uploaded file
     * @return {@code true} when the content type or extension is audio-like
     */
    private boolean isSupportedAudio(final MultipartFile file) {
        final String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            final String normalized = contentType.toLowerCase(Locale.ROOT);
            if (SUPPORTED_AUDIO_PREFIXES.stream().anyMatch(normalized::startsWith)) {
                return true;
            }
        }
        final String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        final String extension = filename.toLowerCase(Locale.ROOT)
                .replaceAll("^.*\\.", "");
        return Set.of("webm", "mp4", "m4a", "ogg", "mp3", "mpeg", "mpga", "wav", "flac")
                .contains(extension);
    }

    /**
     * Parses the Whisper response body into a {@link WhisperTranscription}.
     * <p>
     * Handles the {@code verbose_json} shape (with a top-level duration),
     * the default JSON shape (text only), provider error payloads, and
     * malformed responses. The client-reported duration is used when the
     * provider does not return one.
     * </p>
     *
     * @param responseBody      the raw response body
     * @param fallbackDuration  the duration to use when the provider omits it
     * @return the parsed transcription
     * @throws AiTranscriptionException if the response cannot be parsed or
     *                                  contains no transcript
     */
    WhisperTranscription parseResponse(final String responseBody,
                                       final double fallbackDuration) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new AiTranscriptionException(
                    "Speech-to-text returned an empty response. Please try again.");
        }

        try {
            final JsonNode root = objectMapper.readTree(responseBody);

            final JsonNode errorMessage = root.path("error").path("message");
            if (!errorMessage.isMissingNode() && !errorMessage.asText().isBlank()) {
                throw new AiTranscriptionException(
                        "Speech-to-text failed: " + truncate(errorMessage.asText().trim()));
            }

            final String transcript = root.path("text").asText("").trim();
            if (transcript.isEmpty()) {
                throw new AiTranscriptionException(
                        "No speech was detected in the recording. Please try again.");
            }

            final JsonNode durationNode = root.path("duration");
            final double duration = durationNode.isNumber()
                    ? durationNode.asDouble() : fallbackDuration;

            return new WhisperTranscription(transcript, duration);
        } catch (final AiTranscriptionException e) {
            throw e;
        } catch (final Exception e) {
            log.warn("Failed to parse Whisper response body: {}",
                    truncate(responseBody), e);
            throw new AiTranscriptionException(
                    "Speech-to-text could not process the recording. Please try again.", e);
        }
    }

}
