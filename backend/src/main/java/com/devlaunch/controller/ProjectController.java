package com.devlaunch.controller;

import com.devlaunch.dto.request.CreateProjectRequest;
import com.devlaunch.dto.request.UpdateProjectRequest;
import com.devlaunch.dto.response.ProjectResponse;
import com.devlaunch.service.interfaces.ProjectService;
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
 * REST controller for project record management operations.
 * <p>
 * Exposes endpoints for creating, retrieving, updating, and deleting
 * project records associated with a specific resume owned by the
 * currently authenticated user. All endpoints require a valid JWT
 * access token and verify that the target resume belongs to the
 * authenticated user and that the project record belongs to that
 * resume.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/resumes/{resumeId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Creates a new project record for the specified resume.
     * <p>
     * Accepts the project details, validates the input, delegates
     * creation to {@link ProjectService#createProject(Long, CreateProjectRequest)},
     * and returns the newly created project record data.
     * </p>
     *
     * @param resumeId the ID of the resume to associate the project with
     * @param request  the create-project request containing project details
     * @return a {@link ResponseEntity} containing the created project record
     *         with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @PathVariable final Long resumeId,
            @Valid @RequestBody final CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(resumeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all project records belonging to the specified resume.
     * <p>
     * Delegates to {@link ProjectService#getAllProjects(Long)} to fetch
     * all project records for the specified resume.
     * </p>
     *
     * @param resumeId the ID of the resume whose project records to retrieve
     * @return a {@link ResponseEntity} containing a list of project records
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @PathVariable final Long resumeId) {
        List<ProjectResponse> responses = projectService.getAllProjects(resumeId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a specific project record by its ID within the specified resume.
     * <p>
     * Delegates to {@link ProjectService#getProjectById(Long, Long)} to fetch
     * the project record. The record must belong to the specified resume,
     * and the resume must belong to the authenticated user.
     * </p>
     *
     * @param resumeId  the ID of the resume that owns the project record
     * @param projectId the ID of the project record to retrieve
     * @return a {@link ResponseEntity} containing the project record data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable final Long resumeId,
            @PathVariable final Long projectId) {
        ProjectResponse response = projectService.getProjectById(resumeId, projectId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a specific project record by its ID within the specified resume.
     * <p>
     * Accepts the updated project details, validates the input, and
     * delegates the update to {@link ProjectService#updateProject(Long, Long, UpdateProjectRequest)}.
     * Returns the refreshed project record data after the update. The record
     * must belong to the specified resume, and the resume must belong to the
     * authenticated user.
     * </p>
     *
     * @param resumeId  the ID of the resume that owns the project record
     * @param projectId the ID of the project record to update
     * @param request   the update request containing the new project details
     * @return a {@link ResponseEntity} containing the updated project record
     *         with HTTP status 200 (OK)
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable final Long resumeId,
            @PathVariable final Long projectId,
            @Valid @RequestBody final UpdateProjectRequest request) {
        ProjectResponse response = projectService.updateProject(resumeId, projectId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific project record by its ID within the specified resume.
     * <p>
     * Delegates the deletion to {@link ProjectService#deleteProject(Long, Long)}.
     * The record must belong to the specified resume, and the resume must belong
     * to the authenticated user.
     * </p>
     *
     * @param resumeId  the ID of the resume that owns the project record
     * @param projectId the ID of the project record to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(
            @PathVariable final Long resumeId,
            @PathVariable final Long projectId) {
        projectService.deleteProject(resumeId, projectId);
        return ResponseEntity.ok("Project deleted successfully.");
    }

}
