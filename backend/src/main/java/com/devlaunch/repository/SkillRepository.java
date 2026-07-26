package com.devlaunch.repository;

import com.devlaunch.entity.Resume;
import com.devlaunch.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link Skill} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods
 * for skill-related database access. A resume may contain
 * multiple skill records, so lookup methods return collections.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /**
     * Finds all skill records belonging to the specified resume.
     *
     * @param resume the resume whose skill records to retrieve
     * @return a list of skill records belonging to the resume,
     *         or an empty list if none exist
     */
    List<Skill> findByResume(Resume resume);

}
