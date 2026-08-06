package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for the job application analytics overview.
 * <p>
 * Aggregates the authenticated user's applications into the counts and
 * rates shown in the analytics panel: applications by status, applications
 * per month, interview/offer/rejection/success rates, the average time
 * between applying and reaching an interview, and the currently active and
 * upcoming interviews.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationAnalyticsResponse {

    /**
     * The total number of job applications.
     */
    private Long totalApplications;

    /**
     * The number of applications per status.
     */
    private Map<ApplicationStatus, Long> statusCounts;

    /**
     * The number of applications submitted per month ({@code yyyy-MM}),
     * ordered oldest to newest.
     */
    private List<MonthlyApplicationResponse> monthlyApplications;

    /**
     * The percentage of applications that reached the interview stage,
     * or {@code null} when there are no applications.
     */
    private Double interviewRate;

    /**
     * The percentage of applications that resulted in an offer, or
     * {@code null} when there are no applications.
     */
    private Double offerRate;

    /**
     * The percentage of applications that were rejected, or {@code null}
     * when there are no applications.
     */
    private Double rejectionRate;

    /**
     * The percentage of decided applications (offer or rejected) that
     * ended in an offer, or {@code null} when nothing has been decided yet.
     */
    private Double successRate;

    /**
     * The average number of days between applying and the first interview
     * event, or {@code null} when no interview events exist yet.
     */
    private Double averageResponseTimeDays;

    /**
     * The number of applications currently in the interview stage.
     */
    private Long activeInterviews;

    /**
     * The upcoming (future, non-cancelled) interviews across all
     * applications, ordered by date and time.
     */
    private List<InterviewScheduleResponse> upcomingInterviews;

}
