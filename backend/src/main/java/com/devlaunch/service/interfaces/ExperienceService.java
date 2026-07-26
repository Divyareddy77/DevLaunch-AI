package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.CreateExperienceRequest;
import com.devlaunch.dto.request.UpdateExperienceRequest;
import com.devlaunch.dto.response.ExperienceResponse;
import com.devlaunch.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Service interface for work experience record management operations.
 * <p>
 * Defines the contract for creating, retrieving, updating, and deleting
 * experience records associated with the currently authenticated user's
 * resumes. Each experience record belongs to a specific resume, and all
 * operations verify that the resume belongs to the authenticated user
 * and that the experience record belongs to that resume.
 * </p>
 *
 * @author DevLaunch
 */
public interface ExperienceService {

    /**
     * Creates a new experience record for the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume to associate the experience with
     * @param request  the create-experience request containing professional details
     * @return the newly created experience record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or the resume is not found, or the resume
     *                                   does not belong to the user
     */
    ExperienceResponse createExperience(Long resumeId, CreateExperienceRequest request);

    /**
     * Retrieves all experience records belonging to the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume whose experience records to retrieve
     * @return a list of experience records for the specified resume,
     *         or an empty list if none exist
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or the resume is not found, or the resume
     *                                   does not belong to the user
     */
    List<ExperienceResponse> getAllExperiences(Long resumeId);

    /**
     * Retrieves a specific experience record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId     the ID of the resume that owns the experience record
     * @param experienceId the ID of the experience record to retrieve
     * @return the experience record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the experience
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    ExperienceResponse getExperienceById(Long resumeId, Long experienceId);

    /**
     * Updates a specific experience record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId     the ID of the resume that owns the experience record
     * @param experienceId the ID of the experience record to update
     * @param request      the update request containing the new professional details
     * @return the updated experience record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the experience
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    ExperienceResponse updateExperience(Long resumeId, Long experienceId, UpdateExperienceRequest request);

    /**
     * Deletes a specific experience record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId     the ID of the resume that owns the experience record
     * @param experienceId the ID of the experience record to delete
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the experience
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    void deleteExperience(Long resumeId, Long experienceId);

}
