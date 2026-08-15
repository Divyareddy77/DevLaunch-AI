package com.devlaunch.repository;

import com.devlaunch.entity.InterviewNote;
import com.devlaunch.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link InterviewNote} entity operations.
 *
 * @author DevLaunch
 */
@Repository
public interface InterviewNoteRepository extends JpaRepository<InterviewNote, Long> {

    /**
     * Finds all notes for an application, newest first.
     *
     * @param application the application whose notes to retrieve
     * @return the notes, or an empty list if none exist
     */
    List<InterviewNote> findByApplicationOrderByCreatedAtDesc(JobApplication application);

    /**
     * Finds all notes for a batch of applications.
     * <p>
     * Used by the list endpoint to avoid per-application queries.
     * </p>
     *
     * @param applications the applications whose notes to retrieve
     * @return all matching notes
     */
    List<InterviewNote> findByApplicationIn(List<JobApplication> applications);

}
