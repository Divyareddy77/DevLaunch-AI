package com.devlaunch.dto.request;

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
 * Identifies the interview category the user wants to practise so the
 * AI module can generate an appropriate set of questions.
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

}
