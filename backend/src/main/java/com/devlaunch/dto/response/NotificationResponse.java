package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for a user notification.
 * <p>
 * Exposes the notification fields the notification center displays:
 * a title, the message body, the category, the read state, and the
 * creation timestamp. The owning user is intentionally omitted — the
 * notification APIs always operate on the authenticated user's own
 * notifications.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private String title;

    private String message;

    private NotificationType type;

    private Boolean isRead;

    private LocalDateTime createdAt;

}
