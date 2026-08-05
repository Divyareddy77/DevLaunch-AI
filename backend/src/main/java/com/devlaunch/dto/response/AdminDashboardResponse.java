package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for the admin dashboard overview.
 * <p>
 * Aggregates platform-wide statistics across all DevLaunch modules together
 * with the most recent registrations and interviews. GitHub and LeetCode
 * search counts are intentionally absent because those lookups are not
 * persisted.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    /**
     * Total number of registered users.
     */
    private long totalUsers;

    /**
     * Number of active users.
     */
    private long activeUsers;

    /**
     * Number of inactive (deactivated) users.
     */
    private long inactiveUsers;

    /**
     * Total number of resumes.
     */
    private long totalResumes;

    /**
     * Total number of job applications.
     */
    private long totalJobApplications;

    /**
     * Total number of study plan entries.
     */
    private long totalStudyPlans;

    /**
     * Total number of completed mock interview sessions.
     */
    private long totalInterviewSessions;

    /**
     * Total number of AI resume reviews performed.
     */
    private long totalResumeReviews;

    /**
     * The ten most recently registered users.
     */
    private List<AdminUserResponse> recentRegistrations;

    /**
     * The ten most recently completed mock interviews.
     */
    private List<AdminInterviewSessionResponse> recentInterviews;

}
