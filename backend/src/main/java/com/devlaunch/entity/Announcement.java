package com.devlaunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents an announcement published to DevLaunch users by an administrator.
 * <p>
 * Each record captures a title, the announcement body, whether it is currently
 * active (visible), and the administrator who published it. Announcements are
 * managed exclusively through the admin module and are designed to integrate
 * with the notification system in a future phase.
 * </p>
 *
 * @author DevLaunch
 */
@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "createdBy")
@EqualsAndHashCode(callSuper = true)
public class Announcement extends BaseEntity {

    /**
     * The headline of the announcement.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * The full body of the announcement.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Content is required")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Whether the announcement is currently visible to users.
     * <p>
     * Defaults to {@code true} so newly created announcements go live
     * immediately unless explicitly unpublished.
     * </p>
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = Boolean.TRUE;

    /**
     * The administrator who created or last edited this announcement.
     * <p>
     * An administrator may author many announcements.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

}
