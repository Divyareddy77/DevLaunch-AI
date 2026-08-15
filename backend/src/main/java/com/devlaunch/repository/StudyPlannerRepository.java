package com.devlaunch.repository;

import com.devlaunch.entity.StudyPlanner;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.StudyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for {@link StudyPlanner} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods
 * for study-planner-related database access. A user may have
 * many study planner entries, so lookup methods return collections.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface StudyPlannerRepository extends JpaRepository<StudyPlanner, Long> {

    /**
     * Finds all study planner entries belonging to the specified user.
     *
     * @param user the user whose study planner entries to retrieve
     * @return a list of study planner entries belonging to the user,
     *         or an empty list if none exist
     */
    List<StudyPlanner> findByUser(User user);

    /**
     * Finds all study planner entries whose status is not the given status
     * and whose scheduled date is before the given date.
     * <p>
     * Used by the overdue-reminder scheduler to find tasks that are past
     * their scheduled date and still not completed.
     * </p>
     *
     * @param status the status that must NOT match (typically COMPLETED)
     * @param date   the date before which tasks are considered overdue
     * @return a list of overdue study planner entries
     */
    List<StudyPlanner> findByStatusNotAndStudyDateBefore(StudyStatus status, LocalDate date);

    /**
     * Counts the study planner entries of a user with the given status.
     * <p>
     * Used by the gamification engine to evaluate the study starter
     * achievement (completed task count).
     * </p>
     *
     * @param user   the task owner
     * @param status the status to count (typically COMPLETED)
     * @return the number of matching entries
     */
    long countByUserAndStatus(User user, StudyStatus status);

}
