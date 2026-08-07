package com.devlaunch.messaging.consumer;

import com.devlaunch.entity.User;
import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.MessagingLog;
import com.devlaunch.messaging.event.ActivityEvent;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.GamificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for the gamification activity flow.
 * <p>
 * Receives the lightweight {@link ActivityEvent}s published by the
 * platform modules (resume created, interview completed, study task
 * completed, …), resolves the user, and delegates to the
 * {@link GamificationService} which awards XP, evaluates achievements,
 * unlocks badges, and raises the notifications — all in one transaction.
 * An event for a user that no longer exists is a permanent failure: it is
 * logged and acknowledged so it is never retried or dead-lettered.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class AchievementActivityConsumer {

    private static final Logger log = LoggerFactory.getLogger(AchievementActivityConsumer.class);

    private final GamificationService gamificationService;
    private final UserRepository userRepository;

    /**
     * Constructs the consumer.
     *
     * @param gamificationService the gamification engine (XP + achievements)
     * @param userRepository      repository for user lookups
     */
    public AchievementActivityConsumer(final GamificationService gamificationService,
                                       final UserRepository userRepository) {
        this.gamificationService = gamificationService;
        this.userRepository = userRepository;
    }

    /**
     * Awards XP and evaluates achievements for the activity.
     *
     * @param event the activity event
     */
    @RabbitListener(queues = EventTopics.ACHIEVEMENT_ACTIVITY_QUEUE)
    public void onActivity(final ActivityEvent event) {
        MessagingLog.received(EventTopics.ACHIEVEMENT_ACTIVITY_QUEUE,
                event.getClass().getSimpleName());
        MessagingLog.processingStarted(EventTopics.ACHIEVEMENT_ACTIVITY_QUEUE,
                event.getClass().getSimpleName());

        if (event.userId() == null) {
            log.warn("Skipping activity event without a user id: type={}", event.type());
            return;
        }
        final User user = userRepository.findById(event.userId()).orElse(null);
        if (user == null) {
            log.warn("Skipping activity event for unknown user id={}, type={}",
                    event.userId(), event.type());
            return;
        }

        gamificationService.recordActivity(user, event.type(), event.value());

        MessagingLog.processingCompleted(EventTopics.ACHIEVEMENT_ACTIVITY_QUEUE,
                event.getClass().getSimpleName(),
                "xp + achievements processed for user id=" + user.getId()
                        + ", type=" + event.type());
    }

}
