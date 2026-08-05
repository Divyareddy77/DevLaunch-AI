package com.devlaunch.repository;

import com.devlaunch.entity.Feedback;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link Feedback} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods for
 * feedback-related database access.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * Finds all feedback entries ordered from most recently created to oldest.
     *
     * @return a list of feedback entries, or an empty list if none exist
     */
    List<Feedback> findAllByOrderByCreatedAtDesc();

    /**
     * Finds all feedback entries submitted by the specified user.
     *
     * @param user the user whose feedback to retrieve
     * @return a list of feedback entries belonging to the user,
     *         or an empty list if none exist
     */
    List<Feedback> findByUser(User user);

}
