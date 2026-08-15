package com.devlaunch.dto.request;

import com.devlaunch.entity.enums.InterviewDifficulty;
import com.devlaunch.entity.enums.InterviewType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for starting a new mock interview session.
 * <p>
 * Identifies the interview category the user wants to practise together
 * with the session configuration (difficulty mode, question length, and
 * whether the interview is timed). The AI module uses these options to
 * generate an appropriate set of questions.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewStartRequest {

    /**
     * The category of interview to simulate.
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
     * The number of questions requested for the session, defaulting to 10
     * when not provided.
     */
    private Integer questionLength;

    /**
     * Whether the interview is timed, defaulting to {@code false} when not
     * provided.
     */
    private Boolean timed;

}
