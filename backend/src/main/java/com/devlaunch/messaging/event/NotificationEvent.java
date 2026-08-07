package com.devlaunch.messaging.event;

import com.devlaunch.entity.enums.NotificationType;

/**
 * Lightweight event carrying everything a consumer needs to persist a
 * single user notification.
 * <p>
 * Business services publish this event instead of calling the
 * {@code NotificationService} directly; the consumers persist it through
 * the existing notification module. The title and message are composed by
 * the publishing service (the single owner of the wording) so no message
 * template is ever duplicated between publisher and consumer.
 * </p>
 *
 * @param userId  the ID of the recipient user
 * @param type    the notification category
 * @param title   the notification headline
 * @param message the notification body
 * @author DevLaunch
 */
public record NotificationEvent(Long userId, NotificationType type, String title, String message) {
}
