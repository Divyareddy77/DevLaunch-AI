package com.devlaunch.repository;

import com.devlaunch.entity.Resume;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link Resume} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods
 * for resume-related database access. A user may own multiple
 * resumes, so lookup methods return collections.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    /**
     * Finds all resumes owned by the specified user.
     *
     * @param user the user whose resumes to retrieve
     * @return a list of resumes belonging to the user, or an empty list if none exist
     */
    List<Resume> findByUser(User user);

    /**
     * Counts the resumes owned by the specified user.
     * <p>
     * Used by the gamification engine to evaluate count-based achievements
     * (e.g. Resume Explorer).
     * </p>
     *
     * @param user the user whose resumes to count
     * @return the number of resumes belonging to the user
     */
    long countByUser(User user);

}
