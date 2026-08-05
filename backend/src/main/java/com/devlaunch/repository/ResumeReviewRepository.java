package com.devlaunch.repository;

import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeReview;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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

}
