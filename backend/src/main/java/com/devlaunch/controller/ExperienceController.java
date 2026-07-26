package com.devlaunch.controller;

import com.devlaunch.dto.request.CreateExperienceRequest;
import com.devlaunch.dto.request.UpdateExperienceRequest;
import com.devlaunch.dto.response.ExperienceResponse;
import com.devlaunch.service.interfaces.ExperienceService;
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
 * REST controller for work experience record management operations.
 * <p>
 * Exposes endpoints for creating, retrieving, updating, and deleting
 * experience records associated with a specific resume owned by the
 * currently authenticated user. All endpoints require a valid JWT
 * access token and verify that the target resume belongs to the
 * authenticated user and that the experience record belongs to that
 * resume.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/resumes/{resumeId}/experiences")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;

    /**
     * Creates a new experience record for the specified resume.
     * <p>
     * Accepts the experience details, validates the input, delegates
     * creation to {@link ExperienceService#createExperience(Long, CreateExperienceRequest)},
     * and returns the newly created experience record data.
     * </p>
     *
     * @param resumeId the ID of the resume to associate the experience with
     * @param request  the create-experience request containing professional details
     * @return a {@link ResponseEntity} containing the created experience record
     *         with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<ExperienceResponse> createExperience(
            @PathVariable final Long resumeId,
            @Valid @RequestBody final CreateExperienceRequest request) {
        ExperienceResponse response = experienceService.createExperience(resumeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all experience records belonging to the specified resume.
     * <p>
     * Delegates to {@link ExperienceService#getAllExperiences(Long)} to fetch
     * all experience records for the specified resume.
     * </p>
     *
     * @param resumeId the ID of the resume whose experience records to retrieve
     * @return a {@link ResponseEntity} containing a list of experience records
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<ExperienceResponse>> getAllExperiences(
            @PathVariable final Long resumeId) {
        List<ExperienceResponse> responses = experienceService.getAllExperiences(resumeId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a specific experience record by its ID within the specified resume.
     * <p>
     * Delegates to {@link ExperienceService#getExperienceById(Long, Long)} to fetch
     * the experience record. The record must belong to the specified resume,
     * and the resume must belong to the authenticated user.
     * </p>
     *
     * @param resumeId     the ID of the resume that owns the experience record
     * @param experienceId the ID of the experience record to retrieve
     * @return a {@link ResponseEntity} containing the experience record data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{experienceId}")
    public ResponseEntity<ExperienceResponse> getExperienceById(
            @PathVariable final Long resumeId,
            @PathVariable final Long experienceId) {
        ExperienceResponse response = experienceService.getExperienceById(resumeId, experienceId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a specific experience record by its ID within the specified resume.
     * <p>
     * Accepts the updated experience details, validates the input, and
     * delegates the update to {@link ExperienceService#updateExperience(Long, Long, UpdateExperienceRequest)}.
     * Returns the refreshed experience record data after the update. The record
     * must belong to the specified resume, and the resume must belong to the
     * authenticated user.
     * </p>
     *
     * @param resumeId     the ID of the resume that owns the experience record
     * @param experienceId the ID of the experience record to update
     * @param request      the update request containing the new professional details
     * @return a {@link ResponseEntity} containing the updated experience record
     *         with HTTP status 200 (OK)
     */
    @PutMapping("/{experienceId}")
    public ResponseEntity<ExperienceResponse> updateExperience(
            @PathVariable final Long resumeId,
            @PathVariable final Long experienceId,
            @Valid @RequestBody final UpdateExperienceRequest request) {
        ExperienceResponse response = experienceService.updateExperience(resumeId, experienceId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific experience record by its ID within the specified resume.
     * <p>
     * Delegates the deletion to {@link ExperienceService#deleteExperience(Long, Long)}.
     * The record must belong to the specified resume, and the resume must belong
     * to the authenticated user.
     * </p>
     *
     * @param resumeId     the ID of the resume that owns the experience record
     * @param experienceId the ID of the experience record to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{experienceId}")
    public ResponseEntity<String> deleteExperience(
            @PathVariable final Long resumeId,
            @PathVariable final Long experienceId) {
        experienceService.deleteExperience(resumeId, experienceId);
        return ResponseEntity.ok("Experience deleted successfully.");
    }

}
