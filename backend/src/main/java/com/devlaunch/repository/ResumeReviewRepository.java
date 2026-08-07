package com.devlaunch.repository;

import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeReview;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link ResumeReview} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods for
 * AI resume review history access.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface ResumeReviewRepository extends JpaRepository<ResumeReview, Long> {

    /**
     * Finds all resume reviews performed by the specified user, ordered
     * from most recently created to oldest.
     *
     * @param user the user whose resume reviews to retrieve
     * @return a list of resume reviews belonging to the user,
     *         or an empty list if none exist
     */
    List<ResumeReview> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Finds all resume reviews performed on the specified resume.
     * <p>
     * Used by the admin module to remove review history before a resume
     * is deleted, keeping the {@code resume_reviews.resume_id} foreign
     * key constraint satisfied.
     * </p>
     *
     * @param resume the resume whose reviews to retrieve
     * @return a list of resume reviews on the resume,
     *         or an empty list if none exist
     */
    List<ResumeReview> findByResume(Resume resume);

    /**
     * Counts the resume reviews performed by the specified user.
     * <p>
     * Used by the gamification engine to evaluate the Power User
     * achievement (modules used).
     * </p>
     *
     * @param user the user whose reviews to count
     * @return the number of resume reviews belonging to the user
     */
    long countByUser(User user);

    /**
     * Finds the most recently created review of the specified user.
     * <p>
     * Used by the gamification engine for the latest ATS score of the
     * progress read path.
     * </p>
     *
     * @param user the user whose latest review to retrieve
     * @return the latest review, or an empty {@link Optional}
     */
    Optional<ResumeReview> findFirstByUserOrderByCreatedAtDesc(User user);

}
