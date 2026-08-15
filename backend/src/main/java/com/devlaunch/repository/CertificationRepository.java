package com.devlaunch.repository;

import com.devlaunch.entity.Certification;
import com.devlaunch.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link Certification} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods
 * for certification-related database access. A resume may contain
 * multiple certification records, so lookup methods return collections.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    /**
     * Finds all certification records belonging to the specified resume.
     *
     * @param resume the resume whose certification records to retrieve
     * @return a list of certification records belonging to the resume,
     *         or an empty list if none exist
     */
    List<Certification> findByResume(Resume resume);

}
