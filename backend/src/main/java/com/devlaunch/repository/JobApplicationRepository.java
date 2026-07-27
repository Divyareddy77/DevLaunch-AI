package com.devlaunch.repository;

import com.devlaunch.entity.JobApplication;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link JobApplication} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods
 * for job-application-related database access. A user may have
 * many job applications, so lookup methods return collections.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    /**
     * Finds all job applications belonging to the specified user.
     *
     * @param user the user whose job applications to retrieve
     * @return a list of job applications belonging to the user,
     *         or an empty list if none exist
     */
    List<JobApplication> findByUser(User user);

}
