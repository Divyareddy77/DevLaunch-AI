package com.devlaunch.controller;

import com.devlaunch.dto.request.CreateEducationRequest;
import com.devlaunch.dto.request.UpdateEducationRequest;
import com.devlaunch.dto.response.EducationResponse;
import com.devlaunch.service.interfaces.EducationService;
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
 * REST controller for education record management operations.
 * <p>
 * Exposes endpoints for creating, retrieving, updating, and deleting
 * education records associated with a specific resume owned by the
 * currently authenticated user. All endpoints require a valid JWT
 * access token and verify that the target resume belongs to the
 * authenticated user and that the education record belongs to that
 * resume.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/resumes/{resumeId}/educations")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;

    /**
     * Creates a new education record for the specified resume.
     * <p>
     * Accepts the education details, validates the input, delegates
     * creation to {@link EducationService#createEducation(Long, CreateEducationRequest)},
     * and returns the newly created education record data.
     * </p>
     *
     * @param resumeId the ID of the resume to associate the education with
     * @param request  the create-education request containing academic details
     * @return a {@link ResponseEntity} containing the created education record
     *         with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<EducationResponse> createEducation(
            @PathVariable final Long resumeId,
            @Valid @RequestBody final CreateEducationRequest request) {
        EducationResponse response = educationService.createEducation(resumeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all education records belonging to the specified resume.
     * <p>
     * Delegates to {@link EducationService#getAllEducations(Long)} to fetch
     * all education records for the specified resume.
     * </p>
     *
     * @param resumeId the ID of the resume whose education records to retrieve
     * @return a {@link ResponseEntity} containing a list of education records
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<EducationResponse>> getAllEducations(
            @PathVariable final Long resumeId) {
        List<EducationResponse> responses = educationService.getAllEducations(resumeId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a specific education record by its ID within the specified resume.
     * <p>
     * Delegates to {@link EducationService#getEducationById(Long, Long)} to fetch
     * the education record. The record must belong to the specified resume,
     * and the resume must belong to the authenticated user.
     * </p>
     *
     * @param resumeId    the ID of the resume that owns the education record
     * @param educationId the ID of the education record to retrieve
     * @return a {@link ResponseEntity} containing the education record data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{educationId}")
    public ResponseEntity<EducationResponse> getEducationById(
            @PathVariable final Long resumeId,
            @PathVariable final Long educationId) {
        EducationResponse response = educationService.getEducationById(resumeId, educationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a specific education record by its ID within the specified resume.
     * <p>
     * Accepts the updated education details, validates the input, and
     * delegates the update to {@link EducationService#updateEducation(Long, Long, UpdateEducationRequest)}.
     * Returns the refreshed education record data after the update. The record
     * must belong to the specified resume, and the resume must belong to the
     * authenticated user.
     * </p>
     *
     * @param resumeId    the ID of the resume that owns the education record
     * @param educationId the ID of the education record to update
     * @param request     the update request containing the new academic details
     * @return a {@link ResponseEntity} containing the updated education record
     *         with HTTP status 200 (OK)
     */
    @PutMapping("/{educationId}")
    public ResponseEntity<EducationResponse> updateEducation(
            @PathVariable final Long resumeId,
            @PathVariable final Long educationId,
            @Valid @RequestBody final UpdateEducationRequest request) {
        EducationResponse response = educationService.updateEducation(resumeId, educationId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific education record by its ID within the specified resume.
     * <p>
     * Delegates the deletion to {@link EducationService#deleteEducation(Long, Long)}.
     * The record must belong to the specified resume, and the resume must belong
     * to the authenticated user.
     * </p>
     *
     * @param resumeId    the ID of the resume that owns the education record
     * @param educationId the ID of the education record to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{educationId}")
    public ResponseEntity<String> deleteEducation(
            @PathVariable final Long resumeId,
            @PathVariable final Long educationId) {
        educationService.deleteEducation(resumeId, educationId);
        return ResponseEntity.ok("Education deleted successfully.");
    }

}
