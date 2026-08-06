package com.devlaunch.service.ai;

import com.devlaunch.exception.AiTranscriptionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link OpenAiWhisperTranscriber}.
 * <p>
 * Verifies the Whisper response parsing (verbose JSON with duration, plain
 * JSON fallback, provider error payloads, malformed bodies) and the input
 * validation (empty files, unsupported formats, missing API key). The HTTP
 * transport itself is not exercised here.
 * </p>
 *
 * @author DevLaunch
 */
class OpenAiWhisperTranscriberTest {

    private final OpenAiWhisperTranscriber transcriber = new OpenAiWhisperTranscriber(
            new ObjectMapper(), "test-key", "https://api.openai.com/v1", 10, 300, "whisper-1");

    @Test
    @DisplayName("verbose_json responses return the transcript and duration")
    void parsesVerboseJsonWithDuration() {
        final WhisperTranscription result = transcriber.parseResponse(
                "{\"task\":\"transcribe\",\"language\":\"english\","
                        + "\"duration\":65.25,\"text\":\"I led a team of five engineers.\"}",
                0.0);

        assertEquals("I led a team of five engineers.", result.transcript());
        assertEquals(65.25, result.duration(), 0.001);
    }

    @Test
    @DisplayName("plain JSON responses fall back to the client duration")
    void fallsBackToClientDurationWhenProviderOmitsIt() {
        final WhisperTranscription result = transcriber.parseResponse(
                "{\"text\":\"REST APIs use HTTP methods.\"}", 42.0);

        assertEquals("REST APIs use HTTP methods.", result.transcript());
        assertEquals(42.0, result.duration(), 0.001);
    }

    @Test
    @DisplayName("provider error payloads surface the real provider message")
    void providerErrorsThrowTranscriptionException() {
        final AiTranscriptionException ex = assertThrows(AiTranscriptionException.class,
                () -> transcriber.parseResponse(
                        "{\"error\":{\"message\":\"Invalid file format.\"}}", 0.0));
        assertTrue(ex.getMessage().contains("Invalid file format"),
                "expected the real provider message but was: " + ex.getMessage());
    }

    @Test
    @DisplayName("empty transcripts throw a friendly no-speech exception")
    void emptyTranscriptThrowsNoSpeech() {
        final AiTranscriptionException ex = assertThrows(AiTranscriptionException.class,
                () -> transcriber.parseResponse("{\"text\":\"   \"}", 0.0));
        assertTrue(ex.getMessage().contains("No speech"));
    }

    @Test
    @DisplayName("malformed response bodies throw a transcription exception")
    void malformedResponsesThrowTranscriptionException() {
        assertThrows(AiTranscriptionException.class,
                () -> transcriber.parseResponse("not json", 0.0));
        assertThrows(AiTranscriptionException.class,
                () -> transcriber.parseResponse(null, 0.0));
        assertThrows(AiTranscriptionException.class,
                () -> transcriber.parseResponse("", 0.0));
    }

    @Test
    @DisplayName("empty audio files are rejected as bad input")
    void emptyFilesAreRejected() {
        final MockMultipartFile empty = new MockMultipartFile(
                "file", "answer.webm", "audio/webm", new byte[0]);
        assertThrows(IllegalArgumentException.class,
                () -> transcriber.transcribe(empty, 10));
    }

    @Test
    @DisplayName("non-audio files are rejected as unsupported formats")
    void nonAudioFilesAreRejected() {
        final MockMultipartFile image = new MockMultipartFile(
                "file", "photo.png", "image/png", "not audio".getBytes());
        assertThrows(IllegalArgumentException.class,
                () -> transcriber.transcribe(image, 10));
    }

    @Test
    @DisplayName("an unconfigured transcriber reports the service as unavailable")
    void unconfiguredTranscriberThrowsUnavailable() {
        final OpenAiWhisperTranscriber unconfigured = new OpenAiWhisperTranscriber(
                new ObjectMapper(), "", "https://api.openai.com/v1", 10, 300, "whisper-1");
        final MockMultipartFile audio = new MockMultipartFile(
                "file", "answer.webm", "audio/webm", "audio".getBytes());

        final AiTranscriptionException ex = assertThrows(AiTranscriptionException.class,
                () -> unconfigured.transcribe(audio, 10));
        assertTrue(ex.getMessage().contains("unavailable"));
    }

}
