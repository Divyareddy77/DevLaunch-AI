package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.AnnouncementRequest;
import com.devlaunch.dto.request.FeedbackSubmissionRequest;
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
import com.devlaunch.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * Service interface for the admin module.
 * <p>
 * Defines the contract for platform-wide administrative operations:
 * dashboard statistics, user management, moderation of resumes, job
 * applications and study plans, AI module monitoring, announcement CRUD,
 * and feedback management. Every method is invoked exclusively through
 * {@code /api/admin/**} endpoints protected by the {@code ROLE_ADMIN}
 * authority, with the exception of {@link #submitFeedback} and
 * {@link #getActiveAnnouncements} which are the user-facing feedback
 * submission and announcement reading entry points.
 * </p>
 *
 * @author DevLaunch
 */
public interface AdminService {

    /**
     * Compiles platform-wide statistics and recent activity for the admin
     * dashboard.
     *
     * @return the aggregated dashboard response
     */
    AdminDashboardResponse getDashboard();

    /**
     * Returns a page of users filtered by an optional search term, role,
     * and active status.
     *
     * @param search optional free-text search on name or email
     * @param role   optional role filter
     * @param active optional active-status filter
     * @param page   the zero-based page number
     * @param size   the page size
     * @return a page of admin user responses
     */
    PagedResponse<AdminUserResponse> getUsers(String search, RoleType role,
                                              Boolean active, int page, int size);

    /**
     * Returns the details of a single user.
     *
     * @param id the user id
     * @return the admin user response
     * @throws ResourceNotFoundException if the user does not exist
     */
    AdminUserResponse getUserDetails(Long id);

    /**
     * Activates or deactivates a user account.
     *
     * @param id     the user id
     * @param active the new active state
     * @return the updated admin user response
     * @throws ResourceNotFoundException if the user does not exist
     * @throws IllegalArgumentException  if the target is the current admin
     */
    AdminUserResponse setUserActive(Long id, boolean active);

    /**
     * Permanently deletes a user together with all of their platform data.
     *
     * @param id the user id
     * @throws ResourceNotFoundException if the user does not exist
     * @throws IllegalArgumentException  if the target is the current admin
     */
    void deleteUser(Long id);

    /**
     * Returns a page of all resumes across the platform.
     */
    PagedResponse<AdminResumeResponse> getResumes(int page, int size);

    /**
     * Returns the full details of a single resume.
     *
     * @param id the resume id
     * @return the admin resume response
     * @throws ResourceNotFoundException if the resume does not exist
     */
    AdminResumeResponse getResumeDetails(Long id);

    /**
     * Deletes a resume and its sections, detaching any job applications
     * that reference it.
     *
     * @param id the resume id
     * @throws ResourceNotFoundException if the resume does not exist
     */
    void deleteResume(Long id);

    /**
     * Returns a page of all job applications across the platform.
     */
    PagedResponse<AdminJobApplicationResponse> getJobApplications(int page, int size);

    /**
     * Returns the number of job applications per status across the platform.
     *
     * @return a map of status to count
     */
    Map<ApplicationStatus, Long> getJobApplicationStats();

    /**
     * Deletes a job application.
     *
     * @param id the application id
     * @throws ResourceNotFoundException if the application does not exist
     */
    void deleteJobApplication(Long id);

    /**
     * Returns a page of all study plan entries across the platform.
     */
    PagedResponse<AdminStudyPlannerResponse> getStudyPlans(int page, int size);

    /**
     * Deletes a study plan entry.
     *
     * @param id the study plan id
     * @throws ResourceNotFoundException if the entry does not exist
     */
    void deleteStudyPlan(Long id);

    /**
     * Returns a page of AI resume review history across the platform.
     */
    PagedResponse<AdminResumeReviewResponse> getResumeReviews(int page, int size);

    /**
     * Returns a page of mock interview sessions across the platform,
     * ordered most recent first.
     */
    PagedResponse<AdminInterviewSessionResponse> getInterviewSessions(int page, int size);

    /**
     * Deletes a mock interview session from the platform history.
     *
     * @param id the session id
     * @throws ResourceNotFoundException if the session does not exist
     */
    void deleteInterviewSession(Long id);

    /**
     * Returns all announcements, newest first.
     */
    List<AdminAnnouncementResponse> getAnnouncements();

    /**
     * Returns only active announcements, newest first.
     * <p>
     * This is a user-facing read method; it backs the authenticated
     * {@code GET /api/announcements/active} endpoint. Inactive (unpublished)
     * announcements are never returned.
     * </p>
     *
     * @return the list of active announcements, or an empty list if none exist
     */
    List<AdminAnnouncementResponse> getActiveAnnouncements();

    /**
     * Creates a new announcement authored by the current admin.
     *
     * @param request the announcement payload
     * @return the created announcement
     */
    AdminAnnouncementResponse createAnnouncement(AnnouncementRequest request);

    /**
     * Updates an existing announcement.
     *
     * @param id      the announcement id
     * @param request the updated payload
     * @return the updated announcement
     * @throws ResourceNotFoundException if the announcement does not exist
     */
    AdminAnnouncementResponse updateAnnouncement(Long id, AnnouncementRequest request);

    /**
     * Deletes an announcement.
     *
     * @param id the announcement id
     * @throws ResourceNotFoundException if the announcement does not exist
     */
    void deleteAnnouncement(Long id);

    /**
     * Returns a page of user feedback, newest first.
     */
    PagedResponse<AdminFeedbackResponse> getFeedback(int page, int size);

    /**
     * Deletes a feedback entry.
     *
     * @param id the feedback id
     * @throws ResourceNotFoundException if the feedback does not exist
     */
    void deleteFeedback(Long id);

    /**
     * Submits feedback on behalf of the currently authenticated user.
     * <p>
     * This is the only user-facing method of the admin service; it backs
     * the public {@code POST /api/feedback} endpoint.
     * </p>
     *
     * @param request the feedback payload
     * @throws IllegalArgumentException if the message is blank
     */
    void submitFeedback(FeedbackSubmissionRequest request);

}
