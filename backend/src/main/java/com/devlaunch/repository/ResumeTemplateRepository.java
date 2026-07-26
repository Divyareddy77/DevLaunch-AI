package com.devlaunch.repository;

import com.devlaunch.entity.ResumeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link ResumeTemplate} entity operations.
 * <p>
 * Provides standard CRUD operations for resume template data access.
 * Templates are predefined by the system and seeded at application
 * startup.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface ResumeTemplateRepository extends JpaRepository<ResumeTemplate, Long> {

    /**
     * Checks whether a template with the specified name already exists.
     *
     * @param name the template name to check
     * @return {@code true} if a template with the given name exists, otherwise {@code false}
     */
    boolean existsByName(String name);

}
