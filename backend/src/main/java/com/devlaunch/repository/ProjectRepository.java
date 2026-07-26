package com.devlaunch.repository;

import com.devlaunch.entity.Project;
import com.devlaunch.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link Project} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods
 * for project-related database access. A resume may contain
 * multiple project records, so lookup methods return collections.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Finds all project records belonging to the specified resume.
     *
     * @param resume the resume whose project records to retrieve
     * @return a list of project records belonging to the resume,
     *         or an empty list if none exist
     */
    List<Project> findByResume(Resume resume);

}
