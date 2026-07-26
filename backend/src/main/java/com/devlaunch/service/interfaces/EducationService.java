package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.CreateEducationRequest;
import com.devlaunch.dto.request.UpdateEducationRequest;
import com.devlaunch.dto.response.EducationResponse;
import com.devlaunch.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Service interface for education record management operations.
 * <p>
 * Defines the contract for creating, retrieving, updating, and deleting
 * education records associated with the currently authenticated user's
 * resumes. Each education record belongs to a specific resume, and all
 * operations verify that the resume belongs to the authenticated user
 * and that the education record belongs to that resume.
 * </p>
 *
 * @author DevLaunch
 */
public interface EducationService {

    /**
     * Creates a new education record for the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume to associate the education with
     * @param request  the create-education request containing academic details
     * @return the newly created education record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or the resume is not found, or the resume
     *                                   does not belong to the user
     */
    EducationResponse createEducation(Long resumeId, CreateEducationRequest request);

    /**
     * Retrieves all education records belonging to the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume whose education records to retrieve
     * @return a list of education records for the specified resume,
     *         or an empty list if none exist
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or the resume is not found, or the resume
     *                                   does not belong to the user
     */
    List<EducationResponse> getAllEducations(Long resumeId);

    /**
     * Retrieves a specific education record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId    the ID of the resume that owns the education record
     * @param educationId the ID of the education record to retrieve
     * @return the education record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the education
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    EducationResponse getEducationById(Long resumeId, Long educationId);

    /**
     * Updates a specific education record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId    the ID of the resume that owns the education record
     * @param educationId the ID of the education record to update
     * @param request     the update request containing the new academic details
     * @return the updated education record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the education
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    EducationResponse updateEducation(Long resumeId, Long educationId, UpdateEducationRequest request);

    /**
     * Deletes a specific education record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId    the ID of the resume that owns the education record
     * @param educationId the ID of the education record to delete
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the education
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    void deleteEducation(Long resumeId, Long educationId);

}
