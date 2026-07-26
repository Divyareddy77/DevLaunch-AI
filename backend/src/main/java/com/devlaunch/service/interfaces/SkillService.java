package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.CreateSkillRequest;
import com.devlaunch.dto.request.UpdateSkillRequest;
import com.devlaunch.dto.response.SkillResponse;
import com.devlaunch.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Service interface for skill record management operations.
 * <p>
 * Defines the contract for creating, retrieving, updating, and deleting
 * skill records associated with the currently authenticated user's
 * resumes. Each skill record belongs to a specific resume, and all
 * operations verify that the resume belongs to the authenticated user
 * and that the skill record belongs to that resume.
 * </p>
 *
 * @author DevLaunch
 */
public interface SkillService {

    /**
     * Creates a new skill record for the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume to associate the skill with
     * @param request  the create-skill request containing skill details
     * @return the newly created skill record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or the resume is not found, or the resume
     *                                   does not belong to the user
     */
    SkillResponse createSkill(Long resumeId, CreateSkillRequest request);

    /**
     * Retrieves all skill records belonging to the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume whose skill records to retrieve
     * @return a list of skill records for the specified resume,
     *         or an empty list if none exist
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or the resume is not found, or the resume
     *                                   does not belong to the user
     */
    List<SkillResponse> getAllSkills(Long resumeId);

    /**
     * Retrieves a specific skill record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId the ID of the resume that owns the skill record
     * @param skillId  the ID of the skill record to retrieve
     * @return the skill record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the skill
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    SkillResponse getSkillById(Long resumeId, Long skillId);

    /**
     * Updates a specific skill record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId the ID of the resume that owns the skill record
     * @param skillId  the ID of the skill record to update
     * @param request  the update request containing the new skill details
     * @return the updated skill record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the skill
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    SkillResponse updateSkill(Long resumeId, Long skillId, UpdateSkillRequest request);

    /**
     * Deletes a specific skill record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId the ID of the resume that owns the skill record
     * @param skillId  the ID of the skill record to delete
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the skill
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    void deleteSkill(Long resumeId, Long skillId);

}
