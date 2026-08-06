package com.devlaunch.service.impl;

import com.devlaunch.entity.InterviewSchedule;
import com.devlaunch.entity.Notification;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.repository.InterviewScheduleRepository;
import com.devlaunch.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Schedules job-application related reminders.
 * <p>
 * Every morning the scheduler scans all non-cancelled interviews scheduled
 * for the next day and creates an "interview tomorrow" notification for the
 * owning user through the existing notification module. Notifications are
 * deduplicated per interview so the same reminder is never sent twice.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class JobApplicationNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobApplicationNotificationScheduler.class);

    /**
     * The title used for interview-reminder notifications.
     */
    private static final String INTERVIEW_TOMORROW_TITLE = "Interview tomorrow";

    private final InterviewScheduleRepository interviewScheduleRepository;
    private final NotificationRepository notificationRepository;

    /**
     * Constructs the scheduler with the required repositories.
     *
     * @param interviewScheduleRepository repository for interview schedules
     * @param notificationRepository      repository for notification deduplication
     */
    public JobApplicationNotificationScheduler(
            final InterviewScheduleRepository interviewScheduleRepository,
            final NotificationRepository notificationRepository) {
        this.interviewScheduleRepository = interviewScheduleRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Reminds users about interviews scheduled for tomorrow.
     * <p>
     * Runs at 7:00 AM every day. For each non-cancelled interview scheduled
     * tomorrow, a notification is created for its owner — unless an
     * identical notification was already created today.
     * </p>
     */
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void remindAboutInterviewsTomorrow() {
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        final List<InterviewSchedule> interviewsTomorrow = interviewScheduleRepository
                .findByCancelledFalseAndScheduledDateOrderByScheduledDateAscScheduledTimeAsc(
                        tomorrow);

        for (final InterviewSchedule interview : interviewsTomorrow) {
            final String time = interview.getScheduledTime() == null ? "" : " at "
                    + interview.getScheduledTime().format(DateTimeFormatter.ofPattern("h:mm a"));
            final String message = "Your interview for " + interview.getApplication().getJobRole()
                    + " at " + interview.getApplication().getCompanyName()
                    + " is scheduled for tomorrow" + time + ".";

            if (notificationRepository.existsByUserAndTitleAndMessageAndCreatedAtGreaterThanEqual(
                    interview.getApplication().getUser(), INTERVIEW_TOMORROW_TITLE,
                    message, startOfToday)) {
                continue;
            }

            notificationRepository.save(Notification.builder()
                    .user(interview.getApplication().getUser())
                    .type(NotificationType.JOB)
                    .title(INTERVIEW_TOMORROW_TITLE)
                    .message(message)
                    .isRead(Boolean.FALSE)
                    .build());
        }

        if (!interviewsTomorrow.isEmpty()) {
            log.debug("Processed {} interview reminders for {}", interviewsTomorrow.size(), tomorrow);
        }
    }

}
