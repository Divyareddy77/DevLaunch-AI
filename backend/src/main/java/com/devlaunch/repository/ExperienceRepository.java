package com.devlaunch.repository;

import com.devlaunch.entity.Experience;
import com.devlaunch.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link Experience} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods
 * for experience-related database access. A resume may contain
 * multiple experience records, so lookup methods return collections.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    /**
     * Finds all experience records belonging to the specified resume.
     *
     * @param resume the resume whose experience records to retrieve
     * @return a list of experience records belonging to the resume,
     *         or an empty list if none exist
     */
    List<Experience> findByResume(Resume resume);

}
