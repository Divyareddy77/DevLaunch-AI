package com.devlaunch.repository;

import com.devlaunch.entity.InterviewSchedule;
import com.devlaunch.entity.JobApplication;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for {@link InterviewSchedule} entity operations.
 *
 * @author DevLaunch
 */
@Repository
public interface InterviewScheduleRepository
        extends JpaRepository<InterviewSchedule, Long> {

    /**
     * Finds all interviews for an application, ordered by date then time.
     *
     * @param application the application whose interviews to retrieve
     * @return the ordered interviews, or an empty list if none exist
     */
    List<InterviewSchedule> findByApplicationOrderByScheduledDateAscScheduledTimeAsc(
            JobApplication application);

    /**
     * Finds all interviews for a batch of applications.
     * <p>
     * Used by the list endpoint to avoid per-application queries.
     * </p>
     *
     * @param applications the applications whose interviews to retrieve
     * @return all matching interviews
     */
    List<InterviewSchedule> findByApplicationIn(List<JobApplication> applications);

    /**
     * Finds the future, non-cancelled interviews belonging to a user's
     * applications, ordered by date then time.
     * <p>
     * Used by the dashboard widget, the analytics overview, and the
     * interview-reminder scheduler.
     * </p>
     *
     * @param user        the user whose upcoming interviews to retrieve
     * @param fromDate    the earliest date to include (typically today)
     * @return the ordered upcoming interviews, or an empty list if none exist
     */
    List<InterviewSchedule>
            findByApplication_UserAndCancelledFalseAndScheduledDateGreaterThanEqualOrderByScheduledDateAscScheduledTimeAsc(
                    User user, LocalDate fromDate);

    /**
     * Finds every non-cancelled interview on a specific date across all
     * users.
     * <p>
     * Used by the reminder scheduler to find interviews happening tomorrow
     * for every user in one query.
     * </p>
     *
     * @param date the exact date to look for
     * @return the interviews scheduled on that date, or an empty list
     */
    List<InterviewSchedule> findByCancelledFalseAndScheduledDateOrderByScheduledDateAscScheduledTimeAsc(
            LocalDate date);

}
