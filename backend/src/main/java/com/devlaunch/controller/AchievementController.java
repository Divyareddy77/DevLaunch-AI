package com.devlaunch.controller;

import com.devlaunch.dto.request.CreateAchievementRequest;
import com.devlaunch.dto.request.UpdateAchievementRequest;
import com.devlaunch.dto.response.AchievementResponse;
import com.devlaunch.service.interfaces.AchievementService;
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
 * REST controller for achievement record management operations.
 * <p>
 * Exposes endpoints for creating, retrieving, updating, and deleting
 * achievement records associated with a specific resume owned by the
 * currently authenticated user. All endpoints require a valid JWT
 * access token and verify that the target resume belongs to the
 * authenticated user and that the achievement record belongs to that
 * resume.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/resumes/{resumeId}/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    /**
     * Creates a new achievement record for the specified resume.
     * <p>
     * Accepts the achievement details, validates the input, delegates
     * creation to {@link AchievementService#createAchievement(Long, CreateAchievementRequest)},
     * and returns the newly created achievement record data.
     * </p>
     *
     * @param resumeId the ID of the resume to associate the achievement with
     * @param request  the create-achievement request containing achievement details
     * @return a {@link ResponseEntity} containing the created achievement record
     *         with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<AchievementResponse> createAchievement(
            @PathVariable final Long resumeId,
            @Valid @RequestBody final CreateAchievementRequest request) {
        AchievementResponse response = achievementService.createAchievement(resumeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all achievement records belonging to the specified resume.
     * <p>
     * Delegates to {@link AchievementService#getAllAchievements(Long)} to fetch
     * all achievement records for the specified resume.
     * </p>
     *
     * @param resumeId the ID of the resume whose achievement records to retrieve
     * @return a {@link ResponseEntity} containing a list of achievement records
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<AchievementResponse>> getAllAchievements(
            @PathVariable final Long resumeId) {
        List<AchievementResponse> responses = achievementService.getAllAchievements(resumeId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a specific achievement record by its ID within the specified resume.
     * <p>
     * Delegates to {@link AchievementService#getAchievementById(Long, Long)} to fetch
     * the achievement record. The record must belong to the specified resume,
     * and the resume must belong to the authenticated user.
     * </p>
     *
     * @param resumeId       the ID of the resume that owns the achievement record
     * @param achievementId  the ID of the achievement record to retrieve
     * @return a {@link ResponseEntity} containing the achievement record data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{achievementId}")
    public ResponseEntity<AchievementResponse> getAchievementById(
            @PathVariable final Long resumeId,
            @PathVariable final Long achievementId) {
        AchievementResponse response = achievementService.getAchievementById(resumeId, achievementId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a specific achievement record by its ID within the specified resume.
     * <p>
     * Accepts the updated achievement details, validates the input, and
     * delegates the update to {@link AchievementService#updateAchievement(Long, Long, UpdateAchievementRequest)}.
     * Returns the refreshed achievement record data after the update. The record
     * must belong to the specified resume, and the resume must belong to the
     * authenticated user.
     * </p>
     *
     * @param resumeId       the ID of the resume that owns the achievement record
     * @param achievementId  the ID of the achievement record to update
     * @param request        the update request containing the new achievement details
     * @return a {@link ResponseEntity} containing the updated achievement record
     *         with HTTP status 200 (OK)
     */
    @PutMapping("/{achievementId}")
    public ResponseEntity<AchievementResponse> updateAchievement(
            @PathVariable final Long resumeId,
            @PathVariable final Long achievementId,
            @Valid @RequestBody final UpdateAchievementRequest request) {
        AchievementResponse response = achievementService.updateAchievement(resumeId, achievementId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific achievement record by its ID within the specified resume.
     * <p>
     * Delegates the deletion to {@link AchievementService#deleteAchievement(Long, Long)}.
     * The record must belong to the specified resume, and the resume must belong
     * to the authenticated user.
     * </p>
     *
     * @param resumeId       the ID of the resume that owns the achievement record
     * @param achievementId  the ID of the achievement record to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{achievementId}")
    public ResponseEntity<String> deleteAchievement(
            @PathVariable final Long resumeId,
            @PathVariable final Long achievementId) {
        achievementService.deleteAchievement(resumeId, achievementId);
        return ResponseEntity.ok("Achievement deleted successfully.");
    }

}
