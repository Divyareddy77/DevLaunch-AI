package com.devlaunch.controller;

import com.devlaunch.dto.request.AnnouncementRequest;
import com.devlaunch.dto.response.AdminAnnouncementResponse;
import com.devlaunch.dto.response.AdminDashboardResponse;
import com.devlaunch.dto.response.AdminFeedbackResponse;
import com.devlaunch.dto.response.AdminInterviewSessionResponse;
import com.devlaunch.dto.response.AdminJobApplicationResponse;
import com.devlaunch.dto.response.AdminResumeResponse;
import com.devlaunch.dto.response.AdminResumeReviewResponse;
import com.devlaunch.dto.response.AdminStudyPlannerResponse;
import com.devlaunch.dto.response.AdminUserResponse;
import com.devlaunch.dto.response.PagedResponse;
import com.devlaunch.entity.enums.ApplicationStatus;
import com.devlaunch.entity.enums.RoleType;
import com.devlaunch.service.interfaces.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the admin module.
 * <p>
 * Exposes platform-wide management endpoints for dashboard statistics,
 * user management, moderation of resumes, job applications and study
 * plans, AI module monitoring, announcement CRUD, and feedback
 * management. Every endpoint in this controller is protected by the
 * {@code ROLE_ADMIN} authority configured in {@code SecurityConfig};
 * normal users receive HTTP 403 Forbidden.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Retrieves the admin dashboard statistics and recent activity.
     *
     * @return the aggregated dashboard response
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    // ─── User management ─────────────────────────────────────────────────────

    /**
     * Returns a page of users with optional search, role, and status filters.
     *
     * @param search optional free-text search on name or email
     * @param role   optional role filter
     * @param active optional active-status filter
     * @param page   the zero-based page number
     * @param size   the page size
     * @return a page of users
     */
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<AdminUserResponse>> getUsers(
            @RequestParam(required = false) final String search,
            @RequestParam(required = false) final RoleType role,
            @RequestParam(required = false) final Boolean active,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        return ResponseEntity.ok(adminService.getUsers(search, role, active, page, size));
    }

    /**
     * Returns the details of a single user.
     *
     * @param id the user id
     * @return the user details
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserResponse> getUserDetails(@PathVariable final Long id) {
        return ResponseEntity.ok(adminService.getUserDetails(id));
    }

    /**
     * Activates or deactivates a user account.
     *
     * @param id     the user id
     * @param active the new active state
     * @return the updated user
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> setUserActive(
            @PathVariable final Long id,
            @RequestParam final boolean active) {
        return ResponseEntity.ok(adminService.setUserActive(id, active));
    }

    /**
     * Permanently deletes a user together with all of their platform data.
     *
     * @param id the user id
     * @return a 200 OK response
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable final Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // ─── Resume management ───────────────────────────────────────────────────

    /**
     * Returns a page of all resumes across the platform.
     *
     * @param page the zero-based page number
     * @param size the page size
     * @return a page of resumes
     */
    @GetMapping("/resumes")
    public ResponseEntity<PagedResponse<AdminResumeResponse>> getResumes(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        return ResponseEntity.ok(adminService.getResumes(page, size));
    }

    /**
     * Returns the full details of a single resume.
     *
     * @param id the resume id
     * @return the resume details
     */
    @GetMapping("/resumes/{id}")
    public ResponseEntity<AdminResumeResponse> getResumeDetails(@PathVariable final Long id) {
        return ResponseEntity.ok(adminService.getResumeDetails(id));
    }

    /**
     * Deletes a resume and its sections.
     *
     * @param id the resume id
     * @return a 200 OK response
     */
    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable final Long id) {
        adminService.deleteResume(id);
        return ResponseEntity.ok().build();
    }

    // ─── Job application management ──────────────────────────────────────────

    /**
     * Returns a page of all job applications across the platform.
     *
     * @param page the zero-based page number
     * @param size the page size
     * @return a page of job applications
     */
    @GetMapping("/job-applications")
    public ResponseEntity<PagedResponse<AdminJobApplicationResponse>> getJobApplications(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        return ResponseEntity.ok(adminService.getJobApplications(page, size));
    }

    /**
     * Returns the number of job applications per status.
     *
     * @return a map of status to count
     */
    @GetMapping("/job-applications/stats")
    public ResponseEntity<Map<ApplicationStatus, Long>> getJobApplicationStats() {
        return ResponseEntity.ok(adminService.getJobApplicationStats());
    }

    /**
     * Deletes a job application.
     *
     * @param id the application id
     * @return a 200 OK response
     */
    @DeleteMapping("/job-applications/{id}")
    public ResponseEntity<Void> deleteJobApplication(@PathVariable final Long id) {
        adminService.deleteJobApplication(id);
        return ResponseEntity.ok().build();
    }

    // ─── Study planner management ────────────────────────────────────────────

    /**
     * Returns a page of all study plan entries across the platform.
     *
     * @param page the zero-based page number
     * @param size the page size
     * @return a page of study plan entries
     */
    @GetMapping("/study-plans")
    public ResponseEntity<PagedResponse<AdminStudyPlannerResponse>> getStudyPlans(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        return ResponseEntity.ok(adminService.getStudyPlans(page, size));
    }

    /**
     * Deletes a study plan entry.
     *
     * @param id the study plan id
     * @return a 200 OK response
     */
    @DeleteMapping("/study-plans/{id}")
    public ResponseEntity<Void> deleteStudyPlan(@PathVariable final Long id) {
        adminService.deleteStudyPlan(id);
        return ResponseEntity.ok().build();
    }

    // ─── AI module monitoring ────────────────────────────────────────────────

    /**
     * Returns a page of AI resume review history across the platform.
     *
     * @param page the zero-based page number
     * @param size the page size
     * @return a page of resume review records
     */
    @GetMapping("/ai/resume-reviews")
    public ResponseEntity<PagedResponse<AdminResumeReviewResponse>> getResumeReviews(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        return ResponseEntity.ok(adminService.getResumeReviews(page, size));
    }

    /**
     * Returns a page of mock interview sessions across the platform.
     *
     * @param page the zero-based page number
     * @param size the page size
     * @return a page of interview sessions
     */
    @GetMapping("/ai/interviews")
    public ResponseEntity<PagedResponse<AdminInterviewSessionResponse>> getInterviewSessions(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        return ResponseEntity.ok(adminService.getInterviewSessions(page, size));
    }

    /**
     * Deletes a mock interview session from the platform history.
     *
     * @param id the session id
     * @return a 200 OK response
     */
    @DeleteMapping("/ai/interviews/{id}")
    public ResponseEntity<Void> deleteInterviewSession(@PathVariable final Long id) {
        adminService.deleteInterviewSession(id);
        return ResponseEntity.ok().build();
    }

    // ─── Announcement management ─────────────────────────────────────────────

    /**
     * Returns all announcements, newest first.
     *
     * @return the list of announcements
     */
    @GetMapping("/announcements")
    public ResponseEntity<List<AdminAnnouncementResponse>> getAnnouncements() {
        return ResponseEntity.ok(adminService.getAnnouncements());
    }

    /**
     * Creates a new announcement.
     *
     * @param request the announcement payload
     * @return the created announcement
     */
    @PostMapping("/announcements")
    public ResponseEntity<AdminAnnouncementResponse> createAnnouncement(
            @Valid @RequestBody final AnnouncementRequest request) {
        return ResponseEntity.ok(adminService.createAnnouncement(request));
    }

    /**
     * Updates an existing announcement.
     *
     * @param id      the announcement id
     * @param request the updated payload
     * @return the updated announcement
     */
    @PutMapping("/announcements/{id}")
    public ResponseEntity<AdminAnnouncementResponse> updateAnnouncement(
            @PathVariable final Long id,
            @Valid @RequestBody final AnnouncementRequest request) {
        return ResponseEntity.ok(adminService.updateAnnouncement(id, request));
    }

    /**
     * Deletes an announcement.
     *
     * @param id the announcement id
     * @return a 200 OK response
     */
    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable final Long id) {
        adminService.deleteAnnouncement(id);
        return ResponseEntity.ok().build();
    }

    // ─── Feedback management ─────────────────────────────────────────────────

    /**
     * Returns a page of user feedback, newest first.
     *
     * @param page the zero-based page number
     * @param size the page size
     * @return a page of feedback entries
     */
    @GetMapping("/feedback")
    public ResponseEntity<PagedResponse<AdminFeedbackResponse>> getFeedback(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        return ResponseEntity.ok(adminService.getFeedback(page, size));
    }

    /**
     * Deletes a feedback entry.
     *
     * @param id the feedback id
     * @return a 200 OK response
     */
    @DeleteMapping("/feedback/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable final Long id) {
        adminService.deleteFeedback(id);
        return ResponseEntity.ok().build();
    }

}
