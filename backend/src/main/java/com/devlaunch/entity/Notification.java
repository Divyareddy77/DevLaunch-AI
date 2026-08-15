package com.devlaunch.entity;

import com.devlaunch.entity.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a notification delivered to a user of the DevLaunch platform.
 * <p>
 * Notifications are created automatically by the existing modules
 * (resume builder, job tracker, study planner, AI features, and admin
 * announcements) rather than by manual user action. Each record captures
 * a title, the notification body, its category, and whether the user has
 * read it. A user may have many notifications.
 * </p>
 *
 * @author DevLaunch
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "user")
@EqualsAndHashCode(callSuper = true)
public class Notification extends BaseEntity {

    /**
     * The user this notification belongs to.
     * <p>
     * A user may have many notifications, and every notification is
     * owned by exactly one user.
     * </p>
     */
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The headline of the notification.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * The full body of the notification.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Message is required")
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * The category of the notification (announcement, resume, job,
     * study, mock interview, resume review, or system).
     */
    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private NotificationType type;

    /**
     * Whether the user has read the notification.
     * <p>
     * Defaults to {@code false} so newly created notifications are
     * unread until the user explicitly marks them as read.
     * </p>
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = Boolean.FALSE;

}
