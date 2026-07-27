package com.devlaunch.controller;

import com.devlaunch.dto.request.CreateJobApplicationRequest;
import com.devlaunch.dto.request.UpdateJobApplicationRequest;
import com.devlaunch.dto.response.JobApplicationResponse;
import com.devlaunch.service.interfaces.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for job application tracking operations.
 * <p>
 * Exposes endpoints for creating, retrieving, updating, and deleting
 * the currently authenticated user's job applications. All endpoints
 * require a valid JWT access token and operate exclusively on the
 * authenticated user's own job application data. A user may have
 * many job applications, each tracking the progress of a position
 * they have applied or intend to apply for.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    /**
     * Creates a new job application for the currently authenticated user.
     * <p>
     * Accepts the application details, validates the input, delegates creation
     * to {@link JobApplicationService#createJobApplication(CreateJobApplicationRequest)},
     * and returns the newly created job application data.
     * </p>
     *
     * @param request the create-job-application request containing application details
     * @return a {@link ResponseEntity} containing the created job application data
     *         with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<JobApplicationResponse> createJobApplication(
            @Valid @RequestBody final CreateJobApplicationRequest request) {
        JobApplicationResponse response = jobApplicationService.createJobApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all job applications belonging to the currently authenticated user.
     * <p>
     * Delegates to {@link JobApplicationService#getAllJobApplications()} to fetch all of
     * the authenticated user's job applications.
     * </p>
     *
     * @return a {@link ResponseEntity} containing a list of job application data
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> getAllJobApplications() {
        List<JobApplicationResponse> responses = jobApplicationService.getAllJobApplications();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a specific job application by its ID.
     * <p>
     * Delegates to {@link JobApplicationService#getJobApplicationById(Long)} to fetch the
     * job application. The job application must belong to the authenticated user.
     * </p>
     *
     * @param id the job application ID
     * @return a {@link ResponseEntity} containing the job application data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getJobApplicationById(@PathVariable final Long id) {
        JobApplicationResponse response = jobApplicationService.getJobApplicationById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a specific job application by its ID.
     * <p>
     * Accepts the updated application details, validates the input, and
     * delegates the update to
     * {@link JobApplicationService#updateJobApplication(Long, UpdateJobApplicationRequest)}.
     * Returns the refreshed job application data after the update. The job
     * application must belong to the authenticated user.
     * </p>
     *
     * @param id      the job application ID to update
     * @param request the update request containing the new application details
     * @return a {@link ResponseEntity} containing the updated job application data
     *         with HTTP status 200 (OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> updateJobApplication(
            @PathVariable final Long id,
            @Valid @RequestBody final UpdateJobApplicationRequest request) {
        JobApplicationResponse response = jobApplicationService.updateJobApplication(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific job application by its ID.
     * <p>
     * Delegates the deletion to {@link JobApplicationService#deleteJobApplication(Long)}.
     * The job application must belong to the authenticated user.
     * </p>
     *
     * @param id the job application ID to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJobApplication(@PathVariable final Long id) {
        jobApplicationService.deleteJobApplication(id);
        return ResponseEntity.ok("Job application deleted successfully.");
    }

}
