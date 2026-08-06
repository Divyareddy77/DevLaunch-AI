package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO returned when a voice recording is transcribed.
 * <p>
 * Contains the Whisper-transcribed text and the recorded audio duration
 * in seconds, which the client uses to compute speaking analytics
 * (words per minute, filler frequency, confidence).
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranscribeResponse {

    /**
     * The transcribed text of the recording.
     */
    private String transcript;

    /**
     * The duration of the recorded audio in seconds.
     */
    private double duration;

}
