package com.devlaunch.repository;

import com.devlaunch.entity.InterviewSession;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link InterviewSession} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods for
 * interview-history-related database access. A user may have many
 * interview sessions, so lookup methods return collections.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    /**
     * Finds all interview sessions belonging to the specified user,
     * ordered from most recently completed to oldest.
     *
     * @param user the user whose interview sessions to retrieve
     * @return a list of interview sessions belonging to the user,
     *         or an empty list if none exist
     */
    List<InterviewSession> findByUserOrderByCompletedAtDesc(User user);

    /**
     * Finds the ten most recently completed interview sessions across
     * all users, used for the admin dashboard's recent interviews feed.
     *
     * @return a list of up to ten interview sessions, newest first
     */
    List<InterviewSession> findTop10ByOrderByCompletedAtDesc();

}
