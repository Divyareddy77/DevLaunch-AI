package com.devlaunch.service.impl;

import com.devlaunch.dto.request.CreateJobApplicationRequest;
import com.devlaunch.dto.request.UpdateJobApplicationRequest;
import com.devlaunch.dto.response.JobApplicationResponse;
import com.devlaunch.entity.JobApplication;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.User;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.JobApplicationMapper;
import com.devlaunch.repository.JobApplicationRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.JobApplicationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link JobApplicationService} providing job application
 * creation, retrieval, update, and deletion operations for the currently
 * authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain
 * the authenticated user's email, then delegates persistence and
 * mapping to {@link JobApplicationRepository} and {@link JobApplicationMapper}
 * respectively. A user may have many job applications. Update and
 * delete operations verify that the application belongs to the
 * authenticated user. If a resume is specified, its ownership is
 * also verified before association.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final JobApplicationMapper jobApplicationMapper;

    /**
     * Constructs the job application service with the required dependencies.
     *
     * @param jobApplicationRepository repository for job application data access
     * @param resumeRepository         repository for resume data access
     * @param userRepository           repository for user data access
     * @param jobApplicationMapper     mapper for DTO-entity conversions
     */
    public JobApplicationServiceImpl(final JobApplicationRepository jobApplicationRepository,
                                     final ResumeRepository resumeRepository,
                                     final UserRepository userRepository,
                                     final JobApplicationMapper jobApplicationMapper) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.jobApplicationMapper = jobApplicationMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public JobApplicationResponse createJobApplication(final CreateJobApplicationRequest request) {
        final User user = getAuthenticatedUser();

        // Map request DTO to a new JobApplication entity
        final JobApplication jobApplication = jobApplicationMapper.toJobApplication(request);

        // Associate the job application with the authenticated user
        jobApplication.setUser(user);

        // If a resume ID is provided, fetch and verify ownership before associating
        if (request.getResumeId() != null) {
            final Resume resume = getResumeOwnedByAuthenticatedUser(request.getResumeId());
            jobApplication.setResume(resume);
        }

        // Persist the new job application
        final JobApplication savedJobApplication = jobApplicationRepository.save(jobApplication);

        // Return the job application data
        return jobApplicationMapper.toJobApplicationResponse(savedJobApplication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getAllJobApplications() {
        final User user = getAuthenticatedUser();
        final List<JobApplication> jobApplications = jobApplicationRepository.findByUser(user);
        return jobApplications.stream()
                .map(jobApplicationMapper::toJobApplicationResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public JobApplicationResponse getJobApplicationById(final Long id) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);
        return jobApplicationMapper.toJobApplicationResponse(jobApplication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public JobApplicationResponse updateJobApplication(final Long id,
                                                       final UpdateJobApplicationRequest request) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);

        // Update the editable fields
        jobApplication.setCompanyName(request.getCompanyName());
        jobApplication.setJobRole(request.getJobRole());
        jobApplication.setCompanyLocation(request.getCompanyLocation());
        jobApplication.setJobType(request.getJobType());
        jobApplication.setSalary(request.getSalary());
        jobApplication.setApplicationDate(request.getApplicationDate());
        jobApplication.setStatus(request.getStatus());
        jobApplication.setJobUrl(request.getJobUrl());
        jobApplication.setNotes(request.getNotes());

        // Update the resume association if it has changed
        final Long currentResumeId = jobApplication.getResume() != null
                ? jobApplication.getResume().getId() : null;
        final Long newResumeId = request.getResumeId();

        if (currentResumeId == null && newResumeId == null) {
            // No change — both are null
        } else if (currentResumeId != null && currentResumeId.equals(newResumeId)) {
            // No change — same resume ID
        } else if (newResumeId == null) {
            // Resume association removed
            jobApplication.setResume(null);
        } else {
            // Resume association changed to a different resume
            final Resume resume = getResumeOwnedByAuthenticatedUser(newResumeId);
            jobApplication.setResume(resume);
        }

        // Persist the updated job application
        final JobApplication savedJobApplication = jobApplicationRepository.save(jobApplication);

        // Return the updated job application data
        return jobApplicationMapper.toJobApplicationResponse(savedJobApplication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteJobApplication(final Long id) {
        final JobApplication jobApplication = getJobApplicationOwnedByAuthenticatedUser(id);
        jobApplicationRepository.delete(jobApplication);
    }

    /**
     * Retrieves the currently authenticated user from the database.
     * <p>
     * Extracts the username (email) from the {@link SecurityContextHolder},
     * fetches the corresponding {@link User} entity from the repository,
     * and throws a {@link ResourceNotFoundException} if no matching user
     * is found.
     * </p>
     *
     * @return the authenticated {@link User} entity
     * @throws ResourceNotFoundException if the user is not found in the database
     */
    private User getAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + email + " not found"));
    }

    /**
     * Retrieves a resume by ID and verifies it belongs to the currently
     * authenticated user.
     * <p>
     * Fetches the authenticated user first, then looks up the resume by ID.
     * Throws a {@link ResourceNotFoundException} if the resume does not exist
     * or if it belongs to a different user.
     * </p>
     *
     * @param resumeId the resume ID to retrieve
     * @return the {@link Resume} entity owned by the authenticated user
     * @throws ResourceNotFoundException if the resume is not found or does not
     *                                   belong to the authenticated user
     */
    private Resume getResumeOwnedByAuthenticatedUser(final Long resumeId) {
        final User user = getAuthenticatedUser();
        final Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume with id " + resumeId + " not found"));

        // Verify ownership: the resume must belong to the authenticated user
        if (!resume.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Resume with id " + resumeId + " not found for the authenticated user");
        }

        return resume;
    }

    /**
     * Retrieves a job application by ID and verifies it belongs to the currently
     * authenticated user.
     * <p>
     * Fetches the authenticated user first, then looks up the job application
     * by ID. Throws a {@link ResourceNotFoundException} if the job application
     * does not exist or if it belongs to a different user.
     * </p>
     *
     * @param id the job application ID to retrieve
     * @return the {@link JobApplication} entity owned by the authenticated user
     * @throws ResourceNotFoundException if the job application is not found or
     *                                   does not belong to the authenticated user
     */
    private JobApplication getJobApplicationOwnedByAuthenticatedUser(final Long id) {
        final User user = getAuthenticatedUser();
        final JobApplication jobApplication = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job application with id " + id + " not found"));

        // Verify ownership: the job application must belong to the authenticated user
        if (!jobApplication.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Job application with id " + id + " not found for the authenticated user");
        }

        return jobApplication;
    }

}
