package com.devlaunch.messaging.consumer;

import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeReview;
import com.devlaunch.entity.User;
import com.devlaunch.cache.CacheNames;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.MessagingLog;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.messaging.event.ResumeReviewedEvent;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.ResumeReviewRepository;
import com.devlaunch.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for the resume-review-completed flow.
 * <p>
 * Stores the review history row used by the admin module, then persists the
 * completion notification — and the score-improvement notification when the
 * review beats the previous best — through the shared notification
 * processor. Processing status is logged with structured log lines.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class ResumeReviewCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(ResumeReviewCompletedConsumer.class);

    private final NotificationEventProcessor processor;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeReviewRepository resumeReviewRepository;

    /**
     * Constructs the consumer with the required repositories and processor.
     *
     * @param processor             the shared notification persistence logic
     * @param userRepository        repository for user lookups
     * @param resumeRepository      repository for resume lookups
     * @param resumeReviewRepository repository for review history
     */
    public ResumeReviewCompletedConsumer(final NotificationEventProcessor processor,
                                         final UserRepository userRepository,
                                         final ResumeRepository resumeRepository,
                                         final ResumeReviewRepository resumeReviewRepository) {
        this.processor = processor;
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.resumeReviewRepository = resumeReviewRepository;
    }

    /**
     * Stores the review history and persists the review notifications.
     *
     * @param event the resume review completion event
     */
    @RabbitListener(queues = EventTopics.RESUME_REVIEW_COMPLETED_QUEUE)
    @CacheEvict(cacheNames = {CacheNames.DASHBOARD, CacheNames.RESUME}, key = "#event.userId()")
    public void onResumeReviewed(final ResumeReviewedEvent event) {
        MessagingLog.received(EventTopics.RESUME_REVIEW_COMPLETED_QUEUE,
                event.getClass().getSimpleName());
        MessagingLog.processingStarted(EventTopics.RESUME_REVIEW_COMPLETED_QUEUE,
                event.getClass().getSimpleName());

        final User user = userRepository.findById(event.userId()).orElse(null);
        if (user == null) {
            log.warn("Skipping resume review for unknown user id={}", event.userId());
            return;
        }
        final Resume resume = resumeRepository.findById(event.resumeId()).orElse(null);
        if (resume == null) {
            log.warn("Skipping resume review history for unknown resume id={}", event.resumeId());
            return;
        }

        // Store the review history row consumed by the admin module.
        resumeReviewRepository.save(ResumeReview.builder()
                .user(user)
                .resume(resume)
                .targetRole(event.targetRole())
                .resumeScore(event.resumeScore())
                .atsScore(event.atsScore())
                .build());

        processor.process(new NotificationEvent(user.getId(), NotificationType.RESUME_REVIEW,
                "ATS Resume Review completed",
                "Your ATS Resume Review has been completed. Your resume scored "
                        + event.atsScore() + "/100. Check the report for improvements."));

        if (event.previousResumeScore() != null
                && event.resumeScore() > event.previousResumeScore()) {
            processor.process(new NotificationEvent(user.getId(), NotificationType.RESUME_REVIEW,
                    "Resume score improved",
                    "Your resume score improved from " + event.previousResumeScore()
                            + " to " + event.resumeScore() + ". Great progress!"));
        }

        MessagingLog.processingCompleted(EventTopics.RESUME_REVIEW_COMPLETED_QUEUE,
                event.getClass().getSimpleName(),
                "review history saved for resumeId=" + event.resumeId());
    }

}
