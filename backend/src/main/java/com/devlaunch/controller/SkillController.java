package com.devlaunch.controller;

import com.devlaunch.dto.request.CreateSkillRequest;
import com.devlaunch.dto.request.UpdateSkillRequest;
import com.devlaunch.dto.response.SkillResponse;
import com.devlaunch.service.interfaces.SkillService;
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
 * REST controller for skill record management operations.
 * <p>
 * Exposes endpoints for creating, retrieving, updating, and deleting
 * skill records associated with a specific resume owned by the
 * currently authenticated user. All endpoints require a valid JWT
 * access token and verify that the target resume belongs to the
 * authenticated user and that the skill record belongs to that
 * resume.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/resumes/{resumeId}/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    /**
     * Creates a new skill record for the specified resume.
     * <p>
     * Accepts the skill details, validates the input, delegates
     * creation to {@link SkillService#createSkill(Long, CreateSkillRequest)},
     * and returns the newly created skill record data.
     * </p>
     *
     * @param resumeId the ID of the resume to associate the skill with
     * @param request  the create-skill request containing skill details
     * @return a {@link ResponseEntity} containing the created skill record
     *         with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(
            @PathVariable final Long resumeId,
            @Valid @RequestBody final CreateSkillRequest request) {
        SkillResponse response = skillService.createSkill(resumeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all skill records belonging to the specified resume.
     * <p>
     * Delegates to {@link SkillService#getAllSkills(Long)} to fetch
     * all skill records for the specified resume.
     * </p>
     *
     * @param resumeId the ID of the resume whose skill records to retrieve
     * @return a {@link ResponseEntity} containing a list of skill records
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAllSkills(
            @PathVariable final Long resumeId) {
        List<SkillResponse> responses = skillService.getAllSkills(resumeId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a specific skill record by its ID within the specified resume.
     * <p>
     * Delegates to {@link SkillService#getSkillById(Long, Long)} to fetch
     * the skill record. The record must belong to the specified resume,
     * and the resume must belong to the authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume that owns the skill record
     * @param skillId  the ID of the skill record to retrieve
     * @return a {@link ResponseEntity} containing the skill record data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{skillId}")
    public ResponseEntity<SkillResponse> getSkillById(
            @PathVariable final Long resumeId,
            @PathVariable final Long skillId) {
        SkillResponse response = skillService.getSkillById(resumeId, skillId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a specific skill record by its ID within the specified resume.
     * <p>
     * Accepts the updated skill details, validates the input, and
     * delegates the update to {@link SkillService#updateSkill(Long, Long, UpdateSkillRequest)}.
     * Returns the refreshed skill record data after the update. The record
     * must belong to the specified resume, and the resume must belong to the
     * authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume that owns the skill record
     * @param skillId  the ID of the skill record to update
     * @param request  the update request containing the new skill details
     * @return a {@link ResponseEntity} containing the updated skill record
     *         with HTTP status 200 (OK)
     */
    @PutMapping("/{skillId}")
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable final Long resumeId,
            @PathVariable final Long skillId,
            @Valid @RequestBody final UpdateSkillRequest request) {
        SkillResponse response = skillService.updateSkill(resumeId, skillId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific skill record by its ID within the specified resume.
     * <p>
     * Delegates the deletion to {@link SkillService#deleteSkill(Long, Long)}.
     * The record must belong to the specified resume, and the resume must belong
     * to the authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume that owns the skill record
     * @param skillId  the ID of the skill record to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{skillId}")
    public ResponseEntity<String> deleteSkill(
            @PathVariable final Long resumeId,
            @PathVariable final Long skillId) {
        skillService.deleteSkill(resumeId, skillId);
        return ResponseEntity.ok("Skill deleted successfully.");
    }

}
