package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.CreateProjectRequest;
import com.devlaunch.dto.request.UpdateProjectRequest;
import com.devlaunch.dto.response.ProjectResponse;
import com.devlaunch.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Service interface for project record management operations.
 * <p>
 * Defines the contract for creating, retrieving, updating, and deleting
 * project records associated with the currently authenticated user's
 * resumes. Each project record belongs to a specific resume, and all
 * operations verify that the resume belongs to the authenticated user
 * and that the project record belongs to that resume.
 * </p>
 *
 * @author DevLaunch
 */
public interface ProjectService {

    /**
     * Creates a new project record for the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume to associate the project with
     * @param request  the create-project request containing project details
     * @return the newly created project record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or the resume is not found, or the resume
     *                                   does not belong to the user
     */
    ProjectResponse createProject(Long resumeId, CreateProjectRequest request);

    /**
     * Retrieves all project records belonging to the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume whose project records to retrieve
     * @return a list of project records for the specified resume,
     *         or an empty list if none exist
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or the resume is not found, or the resume
     *                                   does not belong to the user
     */
    List<ProjectResponse> getAllProjects(Long resumeId);

    /**
     * Retrieves a specific project record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId  the ID of the resume that owns the project record
     * @param projectId the ID of the project record to retrieve
     * @return the project record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the project
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    ProjectResponse getProjectById(Long resumeId, Long projectId);

    /**
     * Updates a specific project record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId  the ID of the resume that owns the project record
     * @param projectId the ID of the project record to update
     * @param request   the update request containing the new project details
     * @return the updated project record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the project
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    ProjectResponse updateProject(Long resumeId, Long projectId, UpdateProjectRequest request);

    /**
     * Deletes a specific project record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId  the ID of the resume that owns the project record
     * @param projectId the ID of the project record to delete
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the project
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    void deleteProject(Long resumeId, Long projectId);

}
