package com.devlaunch.repository;

import com.devlaunch.entity.Achievement;
import com.devlaunch.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link Achievement} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods
 * for achievement-related database access. A resume may contain
 * multiple achievement records, so lookup methods return collections.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    /**
     * Finds all achievement records belonging to the specified resume.
     *
     * @param resume the resume whose achievement records to retrieve
     * @return a list of achievement records belonging to the resume,
     *         or an empty list if none exist
     */
    List<Achievement> findByResume(Resume resume);

}
