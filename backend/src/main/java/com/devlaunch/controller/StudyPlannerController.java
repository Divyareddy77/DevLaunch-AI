package com.devlaunch.controller;

import com.devlaunch.dto.request.CreateStudyPlannerRequest;
import com.devlaunch.dto.request.UpdateStudyPlannerRequest;
import com.devlaunch.dto.response.StudyPlannerResponse;
import com.devlaunch.service.interfaces.StudyPlannerService;
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
 * REST controller for study planner management operations.
 * <p>
 * Exposes endpoints for creating, retrieving, updating, and deleting
 * the currently authenticated user's study planner entries. All endpoints
 * require a valid JWT access token and operate exclusively on the
 * authenticated user's own study planner data. A user may have many
 * study planner entries, each representing a scheduled study session
 * or task with a title, description, date, priority, and status.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/study-planners")
@RequiredArgsConstructor
public class StudyPlannerController {

    private final StudyPlannerService studyPlannerService;

    /**
     * Creates a new study planner entry for the currently authenticated user.
     * <p>
     * Accepts the study session details, validates the input, delegates creation
     * to {@link StudyPlannerService#createStudyPlanner(CreateStudyPlannerRequest)},
     * and returns the newly created study planner data.
     * </p>
     *
     * @param request the create-study-planner request containing study session details
     * @return a {@link ResponseEntity} containing the created study planner data
     *         with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<StudyPlannerResponse> createStudyPlanner(
            @Valid @RequestBody final CreateStudyPlannerRequest request) {
        StudyPlannerResponse response = studyPlannerService.createStudyPlanner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all study planner entries belonging to the currently authenticated user.
     * <p>
     * Delegates to {@link StudyPlannerService#getAllStudyPlanners()} to fetch all of
     * the authenticated user's study planner entries.
     * </p>
     *
     * @return a {@link ResponseEntity} containing a list of study planner data
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<StudyPlannerResponse>> getAllStudyPlanners() {
        List<StudyPlannerResponse> responses = studyPlannerService.getAllStudyPlanners();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a specific study planner entry by its ID.
     * <p>
     * Delegates to {@link StudyPlannerService#getStudyPlannerById(Long)} to fetch the
     * study planner entry. The entry must belong to the authenticated user.
     * </p>
     *
     * @param id the study planner entry ID
     * @return a {@link ResponseEntity} containing the study planner data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudyPlannerResponse> getStudyPlannerById(@PathVariable final Long id) {
        StudyPlannerResponse response = studyPlannerService.getStudyPlannerById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a specific study planner entry by its ID.
     * <p>
     * Accepts the updated study session details, validates the input, and
     * delegates the update to
     * {@link StudyPlannerService#updateStudyPlanner(Long, UpdateStudyPlannerRequest)}.
     * Returns the refreshed study planner data after the update. The study
     * planner entry must belong to the authenticated user.
     * </p>
     *
     * @param id      the study planner entry ID to update
     * @param request the update request containing the new study session details
     * @return a {@link ResponseEntity} containing the updated study planner data
     *         with HTTP status 200 (OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudyPlannerResponse> updateStudyPlanner(
            @PathVariable final Long id,
            @Valid @RequestBody final UpdateStudyPlannerRequest request) {
        StudyPlannerResponse response = studyPlannerService.updateStudyPlanner(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific study planner entry by its ID.
     * <p>
     * Delegates the deletion to {@link StudyPlannerService#deleteStudyPlanner(Long)}.
     * The study planner entry must belong to the authenticated user.
     * </p>
     *
     * @param id the study planner entry ID to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudyPlanner(@PathVariable final Long id) {
        studyPlannerService.deleteStudyPlanner(id);
        return ResponseEntity.ok("Study planner deleted successfully.");
    }

}
