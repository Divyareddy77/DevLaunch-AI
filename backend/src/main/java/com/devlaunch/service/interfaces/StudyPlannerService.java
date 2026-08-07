package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.CreateStudyPlannerRequest;
import com.devlaunch.dto.request.UpdateStudyPlannerRequest;
import com.devlaunch.dto.response.StudyPlannerResponse;
import com.devlaunch.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Service interface for study planner management operations.
 * <p>
 * Defines the contract for creating, retrieving, updating, and deleting
 * the currently authenticated user's study planner entries. Each entry
 * represents a scheduled study session or task with a title, description,
 * date, optional time window, priority level, and completion status.
 * Update and delete operations require the entry ID and verify that
 * the entry belongs to the authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
public interface StudyPlannerService {

    /**
     * Creates a new study planner entry for the currently authenticated user.
     *
     * @param request the create-study-planner request containing study session details
     * @return the newly created study planner data
     * @throws ResourceNotFoundException if the authenticated user is not found
     */
    StudyPlannerResponse createStudyPlanner(CreateStudyPlannerRequest request);

    /**
     * Retrieves all study planner entries belonging to the currently authenticated user.
     *
     * @return a list of study planner data for the authenticated user,
     *         or an empty list if none exist
     */
    List<StudyPlannerResponse> getAllStudyPlanners();

    /**
     * Retrieves a specific study planner entry by its ID, ensuring it belongs to the
     * currently authenticated user.
     *
     * @param id the study planner entry ID
     * @return the study planner data
     * @throws ResourceNotFoundException if the study planner entry is not found
     *                                    or does not belong to the user
     */
    StudyPlannerResponse getStudyPlannerById(Long id);

    /**
     * Updates a specific study planner entry by its ID, ensuring it belongs to the
     * currently authenticated user.
     *
     * @param id      the study planner entry ID to update
     * @param request the update request containing the new study session details
     * @return the updated study planner data
     * @throws ResourceNotFoundException if the study planner entry is not found
     *                                    or does not belong to the user
     */
    StudyPlannerResponse updateStudyPlanner(Long id, UpdateStudyPlannerRequest request);

    /**
     * Deletes a specific study planner entry by its ID, ensuring it belongs to the
     * currently authenticated user.
     *
     * @param id the study planner entry ID to delete
     * @throws ResourceNotFoundException if the study planner entry is not found
     *                                    or does not belong to the user
     */
    void deleteStudyPlanner(Long id);

    /**
     * Computes the current consecutive-day completion streak of the
     * authenticated user.
     * <p>
     * A streak continues from today, or from yesterday when today has no
     * completion yet, and counts backwards over consecutive days.
     * Shared with the gamification engine for the consistency badge.
     * </p>
     *
     * @return the length of the current streak in days
     */
    int getCurrentStudyStreak();

}
