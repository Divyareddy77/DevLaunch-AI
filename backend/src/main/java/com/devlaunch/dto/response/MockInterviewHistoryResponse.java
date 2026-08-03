package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for the interview history endpoint.
 * <p>
 * Contains the authenticated user's completed interview sessions, the
 * total number of interviews taken, and the average score across all
 * completed interviews.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewHistoryResponse {

    /**
     * The completed interview sessions, most recent first.
     */
    private List<MockInterviewHistoryItemResponse> history;

    /**
     * The total number of interviews taken.
     */
    private Long totalInterviews;

    /**
     * The average score across all completed interviews, rounded to one
     * decimal place.
     */
    private Double averageScore;

}
