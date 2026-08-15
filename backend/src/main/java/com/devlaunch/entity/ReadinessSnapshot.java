package com.devlaunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a recorded placement readiness score for a user.
 * <p>
 * The dashboard writes one snapshot each time the computed placement
 * readiness score changes, so the readiness card can display the previous
 * score, the score difference, and the last-updated date without storing
 * a full history. The {@code createdAt} timestamp inherited from
 * {@link BaseEntity} marks when the score was last recomputed.
 * </p>
 *
 * @author DevLaunch
 */
@Entity
@Table(name = "readiness_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "user")
@EqualsAndHashCode(callSuper = true)
public class ReadinessSnapshot extends BaseEntity {

    /**
     * The user this snapshot belongs to.
     * <p>
     * A user may have many snapshots over time, and every snapshot is
     * owned by exactly one user.
     * </p>
     */
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The placement readiness score at the time of this snapshot,
     * ranging from 0 to 100.
     */
    @Column(name = "score", nullable = false)
    private int score;

}
