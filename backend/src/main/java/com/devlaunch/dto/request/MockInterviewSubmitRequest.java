package com.devlaunch.dto.request;

import com.devlaunch.entity.enums.InterviewDifficulty;
import com.devlaunch.entity.enums.InterviewType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO for submitting the answers of a mock interview session.
 * <p>
 * Carries the session identifier issued when the interview started, the
 * interview category, the session configuration, and the list of
 * question/answer pairs to evaluate.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewSubmitRequest {

    /**
     * The session identifier returned when the interview was started.
     * <p>
     * Must not be blank and must be at most 64 characters.
     * </p>
     */
    @NotBlank(message = "Session id is required")
    @Size(max = 64, message = "Session id must be at most 64 characters")
    private String sessionId;

    /**
     * The category of the interview being submitted.
     * <p>
     * Must not be null.
     * </p>
     */
    @NotNull(message = "Interview type is required")
    private InterviewType interviewType;

    /**
     * The difficulty mode of the interview, defaulting to
     * {@link InterviewDifficulty#MIXED} when not provided.
     */
    private InterviewDifficulty difficulty;

    /**
     * Whether the interview was timed, defaulting to {@code false}.
     */
    private Boolean timed;

    /**
     * The total time spent on the interview in seconds.
     */
    private Integer durationSeconds;

    /**
     * Optional filler-word count reported by the client's speaking
     * analysis. {@code null} when the client does not provide it — the
     * field is additive and fully backward compatible.
     */
    private Integer fillerCount;

    /**
     * Optional speaking pace (words per minute) reported by the client's
     * speaking analysis. {@code null} when the client does not provide it.
     */
    private Double speakingPace;

    /**
     * The question/answer pairs to evaluate.
     * <p>
     * Must contain at least one answer, and each entry is validated.
     * </p>
     */
    @NotEmpty(message = "At least one answer is required")
    @Valid
    private List<MockInterviewAnswerRequest> answers;

}
