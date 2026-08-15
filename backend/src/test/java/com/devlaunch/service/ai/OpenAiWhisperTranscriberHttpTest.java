package com.devlaunch.service.ai;

import com.devlaunch.exception.AiTranscriptionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HTTP-level tests for {@link OpenAiWhisperTranscriber}.
 * <p>
 * Exercises the real RestClient transport against a local JDK HTTP server
 * to verify the exact request sent to the Whisper endpoint: the URL path,
 * the Bearer Authorization header, the multipart parts (model and file),
 * the content type with boundary, the success response parsing, the
 * provider error surfacing, and the read-timeout behaviour.
 * </p>
 *
 * @author DevLaunch
 */
class OpenAiWhisperTranscriberHttpTest {

    private static final String AUDIO_CONTENT = "fake webm audio payload";

    private HttpServer server;
    private String baseUrl;
    private final List<CapturedRequest> requests = new ArrayList<>();
    private final AtomicReference<ResponseSpec> response = new AtomicReference<>();
    private volatile boolean sleepBeforeRespond;

    /** The request the local server captured. */
    private record CapturedRequest(String path, String authorization,
                                   String contentType, String body) {
    }

    /** The response the local server should send. */
    private record ResponseSpec(int status, String body) {
    }

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            try {
                if (sleepBeforeRespond) {
                    try {
                        Thread.sleep(3000);
                    } catch (final InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
                final byte[] body = exchange.getRequestBody().readAllBytes();
                requests.add(new CapturedRequest(
                        exchange.getRequestURI().getPath(),
                        exchange.getRequestHeaders().getFirst("Authorization"),
                        exchange.getRequestHeaders().getFirst("Content-Type"),
                        new String(body, StandardCharsets.UTF_8)));
                final ResponseSpec spec = response.get();
                final String payload = spec == null ? "{}" : spec.body();
                final byte[] out = payload.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(spec == null ? 200 : spec.status(), out.length);
                exchange.getResponseBody().write(out);
            } catch (final Exception ignored) {
                // The client may already have disconnected (e.g. timeout
                // tests) — nothing to do, the exchange is closed below.
            } finally {
                exchange.close();
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private OpenAiWhisperTranscriber transcriber() {
        return new OpenAiWhisperTranscriber(
                new ObjectMapper(), "test-key", baseUrl, 2, 5, "whisper-1");
    }

    private MockMultipartFile audioFile() {
        return new MockMultipartFile(
                "file", "answer.webm", "audio/webm", AUDIO_CONTENT.getBytes());
    }

    @Test
    @DisplayName("sends the Whisper request with the correct endpoint, auth, and multipart parts")
    void sendsWhisperRequestWithCorrectEndpointAuthAndParts() {
        response.set(new ResponseSpec(200,
                "{\"task\":\"transcribe\",\"language\":\"english\","
                        + "\"duration\":3.5,\"text\":\"Hello world.\"}"));

        final WhisperTranscription result = transcriber().transcribe(audioFile(), 4);

        assertEquals("Hello world.", result.transcript());
        assertEquals(3.5, result.duration(), 0.001);

        final CapturedRequest request = requests.get(0);
        assertEquals("/v1/audio/transcriptions", request.path(),
                "must call the Whisper transcriptions endpoint");
        assertEquals("Bearer test-key", request.authorization(),
                "must authenticate with the configured API key");
        assertTrue(request.contentType() != null
                        && request.contentType().startsWith("multipart/form-data"),
                "multipart content type with boundary was expected but was: "
                        + request.contentType());
        assertTrue(request.body().contains("name=\"model\""),
                "multipart body must include the model field");
        assertTrue(request.body().contains("whisper-1"),
                "multipart body must send model=whisper-1");
        assertTrue(request.body().contains("name=\"file\""),
                "multipart body must include the audio file field");
        assertTrue(request.body().contains("filename=\"answer.webm\""),
                "multipart body must carry the original audio filename");
        assertTrue(request.body().contains(AUDIO_CONTENT),
                "multipart body must contain the audio bytes");
    }

    @Test
    @DisplayName("surfaces the real provider error message for rejected requests")
    void surfacesTheRealProviderErrorMessage() {
        response.set(new ResponseSpec(401,
                "{\"error\":{\"message\":\"Incorrect API key provided: sk-test. "
                        + "You can find your API key at "
                        + "https://platform.openai.com/account/api-keys.\"}}"));

        final AiTranscriptionException ex = assertThrows(AiTranscriptionException.class,
                () -> transcriber().transcribe(audioFile(), 4));

        assertTrue(ex.getMessage().contains("Incorrect API key provided"),
                "expected the real provider message but was: " + ex.getMessage());
    }

    @Test
    @DisplayName("surfaces insufficient-credits errors from the provider")
    void surfacesInsufficientCreditsError() {
        response.set(new ResponseSpec(429,
                "{\"error\":{\"message\":\"You exceeded your current quota, "
                        + "please check your plan and billing details.\"}}"));

        final AiTranscriptionException ex = assertThrows(AiTranscriptionException.class,
                () -> transcriber().transcribe(audioFile(), 4));

        assertTrue(ex.getMessage().contains("quota"),
                "expected the quota message but was: " + ex.getMessage());
    }

    @Test
    @DisplayName("read timeouts produce a friendly timeout message")
    void readTimeoutsProduceAFriendlyMessage() {
        sleepBeforeRespond = true;
        final OpenAiWhisperTranscriber slowTranscriber = new OpenAiWhisperTranscriber(
                new ObjectMapper(), "test-key", baseUrl, 2, 1, "whisper-1");

        final AiTranscriptionException ex = assertThrows(AiTranscriptionException.class,
                () -> slowTranscriber.transcribe(audioFile(), 4));

        assertTrue(ex.getMessage().contains("timed out"),
                "expected a timeout message but was: " + ex.getMessage());
    }

    @Test
    @DisplayName("redirects from a misconfigured endpoint surface an error")
    void redirectsFromAMisconfiguredEndpointSurfaceAnError() {
        response.set(new ResponseSpec(301, ""));

        final AiTranscriptionException ex = assertThrows(AiTranscriptionException.class,
                () -> transcriber().transcribe(audioFile(), 4));

        assertTrue(ex.getMessage().contains("Speech-to-text failed"),
                "expected a clear failure for a redirect but was: " + ex.getMessage());
    }

    @Test
    @DisplayName("an unconfigured transcriber names the missing configuration")
    void unconfiguredTranscriberNamesTheMissingConfig() {
        final OpenAiWhisperTranscriber unconfigured = new OpenAiWhisperTranscriber(
                new ObjectMapper(), "", baseUrl, 2, 5, "whisper-1");

        final AiTranscriptionException ex = assertThrows(AiTranscriptionException.class,
                () -> unconfigured.transcribe(audioFile(), 4));

        assertTrue(ex.getMessage().contains("AI_PROVIDER_API_KEY"),
                "expected the missing-config hint but was: " + ex.getMessage());
    }

}
