package com.devlaunch.repository;

import com.devlaunch.entity.StudyPlanner;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}
