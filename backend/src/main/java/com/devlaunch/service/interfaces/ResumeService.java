package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.CreateResumeRequest;
import com.devlaunch.dto.request.UpdateResumeRequest;
import com.devlaunch.dto.response.ResumeResponse;
import com.devlaunch.dto.response.ResumeTemplateResponse;
import com.devlaunch.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Service interface for resume management operations.
 * <p>
 * Defines the contract for creating, retrieving, updating, and deleting
 * the currently authenticated user's resumes. A user may create multiple
 * resumes, each representing a different role, target company, or career
 * objective. Update and delete operations require the resume ID and verify
 * that the resume belongs to the authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
public interface ResumeService {

    /**
     * Creates a new resume for the currently authenticated user.
     *
     * @param request the create-resume request containing professional details
     * @return the newly created resume data
     * @throws ResourceNotFoundException if the authenticated user is not found
     */
    ResumeResponse createResume(CreateResumeRequest request);

    /**
     * Retrieves all resumes belonging to the currently authenticated user.
     *
     * @return a list of resume data for the authenticated user, or an empty list if none exist
     */
    List<ResumeResponse> getAllResumes();

    /**
     * Retrieves a specific resume by its ID, ensuring it belongs to the
     * currently authenticated user.
     *
     * @param id the resume ID
     * @return the resume data
     * @throws ResourceNotFoundException if the resume is not found
     *                                    or does not belong to the user
     */
    ResumeResponse getResumeById(Long id);

    /**
     * Updates a specific resume by its ID, ensuring it belongs to the
     * currently authenticated user.
     *
     * @param id      the resume ID to update
     * @param request the update request containing the new professional details
     * @return the updated resume data
     * @throws ResourceNotFoundException if the resume is not found
     *                                    or does not belong to the user
     */
    ResumeResponse updateResume(Long id, UpdateResumeRequest request);

    /**
     * Deletes a specific resume by its ID, ensuring it belongs to the
     * currently authenticated user.
     *
     * @param id the resume ID to delete
     * @throws ResourceNotFoundException if the resume is not found
     *                                    or does not belong to the user
     */
    void deleteResume(Long id);

    /**
     * Assigns a predefined template to the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * Both the resume and the template must exist in the system.
     * </p>
     *
     * @param resumeId   the ID of the resume to assign the template to
     * @param templateId the ID of the template to assign
     * @return the updated resume data with the assigned template
     * @throws ResourceNotFoundException if the resume, template, or authenticated
     *                                   user is not found, or if the resume does
     *                                   not belong to the user
     */
    ResumeResponse assignTemplate(Long resumeId, Long templateId);

    /**
     * Retrieves the template currently assigned to the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume whose template to retrieve
     * @return the template data assigned to the resume, or {@code null}
     *         if no template is assigned
     * @throws ResourceNotFoundException if the resume or authenticated user
     *                                   is not found, or if the resume does
     *                                   not belong to the user
     */
    ResumeTemplateResponse getResumeTemplate(Long resumeId);

}
