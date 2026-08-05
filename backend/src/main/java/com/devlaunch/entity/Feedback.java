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
 * Represents feedback submitted by a registered user about the platform.
 * <p>
 * Each record captures the feedback message and the user who submitted it.
 * Feedback is reviewed and moderated by administrators through the admin
 * module, which may view or delete entries.
 * </p>
 *
 * @author DevLaunch
 */
@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "user")
@EqualsAndHashCode(callSuper = true)
public class Feedback extends BaseEntity {

    /**
     * The feedback message written by the user.
     * <p>
     * Must not be blank.
     * </p>
     */
    @NotBlank(message = "Feedback message is required")
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * The user who submitted this feedback.
     * <p>
     * A user may submit many feedback entries.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
