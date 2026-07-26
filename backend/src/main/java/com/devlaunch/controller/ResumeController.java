package com.devlaunch.controller;

import com.devlaunch.dto.request.CreateResumeRequest;
import com.devlaunch.dto.request.UpdateResumeRequest;
import com.devlaunch.dto.response.ResumeResponse;
import com.devlaunch.service.interfaces.ResumeService;
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
 * REST controller for resume management operations.
 * <p>
 * Exposes endpoints for creating, retrieving, updating, and deleting
 * the currently authenticated user's resumes. All endpoints require a
 * valid JWT access token and operate exclusively on the authenticated
 * user's own resume data. A user may create multiple resumes.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * Creates a new resume for the currently authenticated user.
     * <p>
     * Accepts the resume details, validates the input, delegates creation
     * to {@link ResumeService#createResume(CreateResumeRequest)}, and
     * returns the newly created resume data.
     * </p>
     *
     * @param request the create-resume request containing professional details
     * @return a {@link ResponseEntity} containing the created resume data
     *         with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(
            @Valid @RequestBody final CreateResumeRequest request) {
        ResumeResponse response = resumeService.createResume(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all resumes belonging to the currently authenticated user.
     * <p>
     * Delegates to {@link ResumeService#getAllResumes()} to fetch all of
     * the authenticated user's resumes.
     * </p>
     *
     * @return a {@link ResponseEntity} containing a list of resume data
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getAllResumes() {
        List<ResumeResponse> responses = resumeService.getAllResumes();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a specific resume by its ID.
     * <p>
     * Delegates to {@link ResumeService#getResumeById(Long)} to fetch the
     * resume. The resume must belong to the authenticated user.
     * </p>
     *
     * @param id the resume ID
     * @return a {@link ResponseEntity} containing the resume data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResumeById(@PathVariable final Long id) {
        ResumeResponse response = resumeService.getResumeById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a specific resume by its ID.
     * <p>
     * Accepts the updated resume details, validates the input, and
     * delegates the update to {@link ResumeService#updateResume(Long, UpdateResumeRequest)}.
     * Returns the refreshed resume data after the update. The resume must
     * belong to the authenticated user.
     * </p>
     *
     * @param id      the resume ID to update
     * @param request the update request containing the new professional details
     * @return a {@link ResponseEntity} containing the updated resume data
     *         with HTTP status 200 (OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResumeResponse> updateResume(
            @PathVariable final Long id,
            @Valid @RequestBody final UpdateResumeRequest request) {
        ResumeResponse response = resumeService.updateResume(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific resume by its ID.
     * <p>
     * Delegates the deletion to {@link ResumeService#deleteResume(Long)}.
     * The resume must belong to the authenticated user.
     * </p>
     *
     * @param id the resume ID to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteResume(@PathVariable final Long id) {
        resumeService.deleteResume(id);
        return ResponseEntity.ok("Resume deleted successfully.");
    }

}
