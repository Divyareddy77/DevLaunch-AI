package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.CreateJobApplicationRequest;
import com.devlaunch.dto.request.UpdateJobApplicationRequest;
import com.devlaunch.dto.response.JobApplicationResponse;
import com.devlaunch.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Service interface for job application tracking operations.
 * <p>
 * Defines the contract for creating, retrieving, updating, and deleting
 * the currently authenticated user's job applications. Each application
 * tracks the company and role applied to, the current status, and
 * optional details such as location, salary, and notes. An application
 * may optionally be linked to the resume used when applying. Update
 * and delete operations require the application ID and verify that
 * the application belongs to the authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
public interface JobApplicationService {

    /**
     * Creates a new job application for the currently authenticated user.
     * <p>
     * If a resume ID is provided in the request, the resume is fetched
     * and verified to belong to the authenticated user before being
     * associated with the application.
     * </p>
     *
     * @param request the create-job-application request containing application details
     * @return the newly created job application data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or if the specified resume is not found
     *                                   or does not belong to the user
     */
    JobApplicationResponse createJobApplication(CreateJobApplicationRequest request);

    /**
     * Retrieves all job applications belonging to the currently authenticated user.
     *
     * @return a list of job application data for the authenticated user,
     *         or an empty list if none exist
     */
    List<JobApplicationResponse> getAllJobApplications();

    /**
     * Retrieves a specific job application by its ID, ensuring it belongs to the
     * currently authenticated user.
     *
     * @param id the job application ID
     * @return the job application data
     * @throws ResourceNotFoundException if the job application is not found
     *                                    or does not belong to the user
     */
    JobApplicationResponse getJobApplicationById(Long id);

    /**
     * Updates a specific job application by its ID, ensuring it belongs to the
     * currently authenticated user.
     * <p>
     * If the resume ID in the request differs from the current value, the
     * resume association is updated accordingly — {@code null} removes the
     * association, and a non-null value fetches and verifies the resume
     * before associating it.
     * </p>
     *
     * @param id      the job application ID to update
     * @param request the update request containing the new application details
     * @return the updated job application data
     * @throws ResourceNotFoundException if the job application is not found
     *                                    or does not belong to the user, or if
     *                                    the specified resume is not found or
     *                                    does not belong to the user
     */
    JobApplicationResponse updateJobApplication(Long id, UpdateJobApplicationRequest request);

    /**
     * Deletes a specific job application by its ID, ensuring it belongs to the
     * currently authenticated user.
     *
     * @param id the job application ID to delete
     * @throws ResourceNotFoundException if the job application is not found
     *                                    or does not belong to the user
     */
    void deleteJobApplication(Long id);

}
