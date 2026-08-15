package com.devlaunch.repository;

import com.devlaunch.entity.ApplicationAttachment;
import com.devlaunch.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link ApplicationAttachment} entity operations.
 *
 * @author DevLaunch
 */
@Repository
public interface ApplicationAttachmentRepository
        extends JpaRepository<ApplicationAttachment, Long> {

    /**
     * Finds all attachments for an application, newest first.
     *
     * @param application the application whose attachments to retrieve
     * @return the attachments, or an empty list if none exist
     */
    List<ApplicationAttachment> findByApplicationOrderByCreatedAtDesc(JobApplication application);

    /**
     * Finds all attachments for a batch of applications.
     * <p>
     * Used by the list endpoint to avoid per-application queries.
     * </p>
     *
     * @param applications the applications whose attachments to retrieve
     * @return all matching attachments
     */
    List<ApplicationAttachment> findByApplicationIn(List<JobApplication> applications);

}
