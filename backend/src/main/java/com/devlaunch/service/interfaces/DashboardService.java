package com.devlaunch.service.interfaces;

import com.devlaunch.dto.response.DashboardResponse;

/**
 * Service interface for the achievement dashboard aggregation operations.
 * <p>
 * Defines the contract for compiling a comprehensive dashboard summary of
 * the currently authenticated user's activity across the DevLaunch platform,
 * including resume data, job applications, study tasks, GitHub statistics,
 * and LeetCode progress. LeetCode metrics are optional and depend on a
 * linked LeetCode account — they will be populated when account linking is
 * added to the user profile in a future phase. All data is derived from
 * existing repositories and services — no new database tables or entities
 * are required.
 * </p>
 *
 * @author DevLaunch
 */
public interface DashboardService {

    /**
     * Builds a complete dashboard summary for the currently authenticated user.
     * <p>
     * Aggregates key metrics from across the platform:
     * <ul>
     *   <li>Resume completion percentage based on the most complete resume</li>
     *   <li>Total job applications and those at the interview stage</li>
     *   <li>Total and completed study planner tasks</li>
     *   <li>GitHub repository count and primary language (via the GitHub API)</li>
     *   <li>LeetCode problems solved (via the LeetCode GraphQL API)</li>
     *   <li>An overall placement readiness score (0–100) computed from the above</li>
     * </ul>
     * </p>
     *
     * @return a {@link DashboardResponse} containing all aggregated metrics
     *         for the authenticated user, with defaults (0 or {@code null}) for
     *         any modules in which the user has no activity or no linked accounts
     */
    DashboardResponse getDashboard();

}
