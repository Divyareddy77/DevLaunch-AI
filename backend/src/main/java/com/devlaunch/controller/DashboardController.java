package com.devlaunch.controller;

import com.devlaunch.dto.response.DashboardResponse;
import com.devlaunch.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the achievement dashboard aggregation operations.
 * <p>
 * Exposes a single endpoint for retrieving a comprehensive dashboard
 * summary of the currently authenticated user's activity across the
 * DevLaunch platform. The dashboard compiles metrics from resume data,
 * job applications, study tasks, GitHub statistics, and LeetCode
 * progress into a single response object. All endpoints require a
 * valid JWT access token.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Retrieves the full achievement dashboard summary for the currently
     * authenticated user.
     * <p>
     * Delegates to {@link DashboardService#getDashboard()} to compile
     * aggregate metrics across all platform modules and returns the
     * resulting dashboard data. Users with no activity in a given
     * module will receive default values (0 or {@code null}) for the
     * corresponding fields.
     * </p>
     *
     * @return a {@link ResponseEntity} containing the dashboard summary
     *         data with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        DashboardResponse response = dashboardService.getDashboard();
        return ResponseEntity.ok(response);
    }

}
