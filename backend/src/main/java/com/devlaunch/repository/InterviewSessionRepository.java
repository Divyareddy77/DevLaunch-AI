package com.devlaunch.repository;

import com.devlaunch.entity.InterviewSession;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
     * Counts the interview sessions belonging to the specified user.
     *
     * @param user the user whose interview sessions to count
     * @return the number of interview sessions belonging to the user
     */
    long countByUser(User user);

    /**
     * Computes the average overall score across the user's interview
     * sessions.
     * <p>
     * Used by the gamification engine to evaluate the Communication Pro
     * achievement (average score &gt;= 80).
     * </p>
     *
     * @param user the user whose sessions to average
     * @return the average overall score (0 when the user has no sessions)
     */
    @Query("SELECT COALESCE(AVG(s.overallScore), 0) FROM InterviewSession s WHERE s.user = :user")
    double averageScoreByUser(@Param("user") User user);

    /**
     * Finds a session by its client-generated identifier, verifying it
     * belongs to the specified user.
     *
     * @param sessionId the client-generated session identifier
     * @param user      the session owner
     * @return the matching session, or an empty {@link Optional}
     */
    Optional<InterviewSession> findBySessionIdAndUser(String sessionId, User user);

    /**
     * Finds the ten most recently completed interview sessions across
     * all users, used for the admin dashboard's recent interviews feed.
     *
     * @return a list of up to ten interview sessions, newest first
     */
    List<InterviewSession> findTop10ByOrderByCompletedAtDesc();

}
