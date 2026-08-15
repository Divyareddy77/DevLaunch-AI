package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.InterviewDifficulty;
import com.devlaunch.entity.enums.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO returned when a mock interview session starts.
 * <p>
 * Contains the generated session identifier, the interview category, the
 * resolved difficulty mode, and the list of questions for the user to
 * answer.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewStartResponse {

    /**
     * The unique session identifier for this interview.
     */
    private String sessionId;

    /**
     * The category of this interview session.
     */
    private InterviewType interviewType;

    /**
     * The resolved difficulty mode of this session.
     */
    private InterviewDifficulty difficulty;

    /**
     * Whether this session is timed.
     */
    private Boolean timed;

    /**
     * The questions generated for this session.
     */
    private List<MockInterviewQuestionResponse> questions;

}
