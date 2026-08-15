package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for the interview history endpoint.
 * <p>
 * Contains the authenticated user's completed interview sessions plus the
 * aggregate statistics that power the landing page hero and the analytics
 * view: best score, average score, last interview date, current streak,
 * most practised category, total time spent, questions answered, success
 * rate, readiness level, and the recent score trend.
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

    /**
     * The best score achieved across all completed interviews, or {@code null}
     * when no interview has been completed yet.
     */
    private Integer bestScore;

    /**
     * The date of the most recent interview, or {@code null} when no
     * interview has been completed yet.
     */
    private LocalDateTime lastInterviewAt;

    /**
     * The number of consecutive days (ending today or yesterday) with at
     * least one completed interview.
     */
    private Integer currentStreak;

    /**
     * The category practised most often, or {@code null} when no interview
     * has been completed yet.
     */
    private InterviewType mostPracticedCategory;

    /**
     * The total time spent across all interviews in seconds.
     */
    private Long totalTimeSpentSeconds;

    /**
     * The total number of questions answered across all interviews.
     */
    private Long totalQuestionsAnswered;

    /**
     * The percentage of interviews scoring 70 or higher, rounded to one
     * decimal place.
     */
    private Double successRate;

    /**
     * A human-readable interview readiness level derived from the total
     * interviews and average score (e.g. "Getting Started", "Interview
     * Ready").
     */
    private String readinessLevel;

    /**
     * The overall score of the ten most recent interviews, newest first.
     */
    private List<ScoreTrendPoint> scoreTrend;

}
