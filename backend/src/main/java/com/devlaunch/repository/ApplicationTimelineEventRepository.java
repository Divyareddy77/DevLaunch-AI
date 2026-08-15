package com.devlaunch.repository;

import com.devlaunch.entity.ApplicationTimelineEvent;
import com.devlaunch.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link ApplicationTimelineEvent} entity operations.
 *
 * @author DevLaunch
 */
@Repository
public interface ApplicationTimelineEventRepository
        extends JpaRepository<ApplicationTimelineEvent, Long> {

    /**
     * Finds all timeline events for an application, oldest first.
     *
     * @param application the application whose timeline to retrieve
     * @return the ordered timeline events, or an empty list if none exist
     */
    List<ApplicationTimelineEvent> findByApplicationOrderByOccurredAtAsc(JobApplication application);

    /**
     * Finds all timeline events for a batch of applications.
     * <p>
     * Used by the list endpoint to avoid per-application queries.
     * </p>
     *
     * @param applications the applications whose events to retrieve
     * @return all matching timeline events
     */
    List<ApplicationTimelineEvent> findByApplicationIn(List<JobApplication> applications);

}
