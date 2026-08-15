package com.devlaunch.service.ai;

/**
 * Internal result of a Whisper speech-to-text transcription.
 * <p>
 * This record is produced by {@link OpenAiWhisperTranscriber} and
 * translated into the public {@code TranscribeResponse} DTO by the AI
 * service layer, so the Whisper integration stays reusable and the
 * provider details never leak into the controller.
 * </p>
 *
 * @param transcript the transcribed text
 * @param duration   the duration of the audio in seconds
 * @author DevLaunch
 */
public record WhisperTranscription(String transcript, double duration) {
}
