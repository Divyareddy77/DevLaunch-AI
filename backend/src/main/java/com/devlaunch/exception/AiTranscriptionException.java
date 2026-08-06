package com.devlaunch.exception;

/**
 * Thrown when speech-to-text transcription fails.
 * <p>
 * Indicates an upstream problem with the transcription provider (an API
 * error, a network timeout, or an unsupported/invalid audio payload) that
 * the caller should surface to the user as a friendly "try again" message
 * rather than a generic server error. Mapped to HTTP 502 Bad Gateway by
 * the {@link GlobalExceptionHandler}.
 * </p>
 *
 * @author DevLaunch
 */
public class AiTranscriptionException extends RuntimeException {

    /**
     * Creates the exception with a human-readable message.
     *
     * @param message the user-facing error message
     */
    public AiTranscriptionException(final String message) {
        super(message);
    }

    /**
     * Creates the exception with a message and the underlying cause.
     *
     * @param message the user-facing error message
     * @param cause   the underlying failure (e.g. network timeout)
     */
    public AiTranscriptionException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
