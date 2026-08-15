package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.CreateAchievementRequest;
import com.devlaunch.dto.request.UpdateAchievementRequest;
import com.devlaunch.dto.response.AchievementResponse;
import com.devlaunch.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Service interface for achievement record management operations.
 * <p>
 * Defines the contract for creating, retrieving, updating, and deleting
 * achievement records associated with the currently authenticated user's
 * resumes. Each achievement record belongs to a specific resume, and all
 * operations verify that the resume belongs to the authenticated user
 * and that the achievement record belongs to that resume.
 * </p>
 *
 * @author DevLaunch
 */
public interface AchievementService {

    /**
     * Creates a new achievement record for the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume to associate the achievement with
     * @param request  the create-achievement request containing achievement details
     * @return the newly created achievement record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or the resume is not found, or the resume
     *                                   does not belong to the user
     */
    AchievementResponse createAchievement(Long resumeId, CreateAchievementRequest request);

    /**
     * Retrieves all achievement records belonging to the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume whose achievement records to retrieve
     * @return a list of achievement records for the specified resume,
     *         or an empty list if none exist
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or the resume is not found, or the resume
     *                                   does not belong to the user
     */
    List<AchievementResponse> getAllAchievements(Long resumeId);

    /**
     * Retrieves a specific achievement record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId       the ID of the resume that owns the achievement record
     * @param achievementId  the ID of the achievement record to retrieve
     * @return the achievement record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the achievement
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    AchievementResponse getAchievementById(Long resumeId, Long achievementId);

    /**
     * Updates a specific achievement record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId       the ID of the resume that owns the achievement record
     * @param achievementId  the ID of the achievement record to update
     * @param request        the update request containing the new achievement details
     * @return the updated achievement record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the achievement
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    AchievementResponse updateAchievement(Long resumeId, Long achievementId, UpdateAchievementRequest request);

    /**
     * Deletes a specific achievement record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId       the ID of the resume that owns the achievement record
     * @param achievementId  the ID of the achievement record to delete
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the achievement
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    void deleteAchievement(Long resumeId, Long achievementId);

}
