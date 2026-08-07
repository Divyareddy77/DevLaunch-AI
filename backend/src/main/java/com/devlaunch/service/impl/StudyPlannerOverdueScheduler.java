package com.devlaunch.service.impl;

import com.devlaunch.entity.StudyPlanner;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.StudyStatus;
import com.devlaunch.messaging.EventPublisher;
import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.StudyTaskEvent;
import com.devlaunch.messaging.event.StudyTaskType;
import com.devlaunch.repository.NotificationRepository;
import com.devlaunch.repository.StudyPlannerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Publishes overdue study task events.
 * <p>
 * Every morning the scheduler scans all study tasks that are past their
 * scheduled date and still not completed, and publishes an overdue event for
 * each so the messaging consumer can raise the reminder notification.
 * Reminders are deduplicated per task per day (the scheduler owns the
 * message wording, matching the interview-reminder scheduler pattern), so
 * the same task is never reminded twice in one day.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class StudyPlannerOverdueScheduler {

    private static final Logger log = LoggerFactory.getLogger(StudyPlannerOverdueScheduler.class);

    /**
     * The title used for overdue-task notifications.
     */
    private static final String OVERDUE_TITLE = "Study task overdue";

    private final StudyPlannerRepository studyPlannerRepository;
    private final NotificationRepository notificationRepository;
    private final EventPublisher eventPublisher;

    /**
     * Constructs the scheduler with the required repositories.
     *
     * @param studyPlannerRepository repository for study planner data access
     * @param notificationRepository repository for reminder deduplication
     * @param eventPublisher         publisher for the messaging backbone
     */
    public StudyPlannerOverdueScheduler(final StudyPlannerRepository studyPlannerRepository,
                                        final NotificationRepository notificationRepository,
                                        final EventPublisher eventPublisher) {
        this.studyPlannerRepository = studyPlannerRepository;
        this.notificationRepository = notificationRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes overdue-task events for tasks past their scheduled date.
     * <p>
     * Runs at 9:00 AM every day. Each overdue task is published once per
     * day; the message is owned by this scheduler so the deduplication
     * check and the delivered notification always match.
     * </p>
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void remindAboutOverdueTasks() {
        final LocalDate today = LocalDate.now();
        final LocalDateTime startOfToday = today.atStartOfDay();

        final List<StudyPlanner> overdueTasks =
                studyPlannerRepository.findByStatusNotAndStudyDateBefore(StudyStatus.COMPLETED, today);

        for (final StudyPlanner task : overdueTasks) {
            final User user = task.getUser();
            final String message = "Task \"" + task.getTitle() + "\" was due on "
                    + task.getStudyDate() + " and is still pending. Try to complete it today!";

            if (notificationRepository.existsByUserAndTitleAndMessageAndCreatedAtGreaterThanEqual(
                    user, OVERDUE_TITLE, message, startOfToday)) {
                continue;
            }

            eventPublisher.publish(EventTopics.STUDY_REMINDER_KEY,
                    new StudyTaskEvent(user.getId(), task.getId(), task.getTitle(),
                            task.getStudyDate(), StudyTaskType.OVERDUE, null, null, message));
        }

        if (!overdueTasks.isEmpty()) {
            log.debug("Published {} overdue study task reminders for {}", overdueTasks.size(), today);
        }
    }

}
