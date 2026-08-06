package com.devlaunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

/**
 * Represents a single-use password reset token issued when a user
 * requests a password reset.
 * <p>
 * Tokens are generated with a cryptographically secure random source,
 * expire after a short window (30 minutes by default), and are marked
 * as used as soon as they are consumed so a link can never be replayed.
 * A user may have at most one outstanding (unused) token at a time —
 * requesting a new reset invalidates any previous one.
 * </p>
 *
 * @author DevLaunch
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "user")
@EqualsAndHashCode(callSuper = true)
public class PasswordResetToken extends BaseEntity {

    /**
     * The opaque token value included in the reset link.
     * <p>
     * Stored in plain form so it can be looked up on reset, but it is
     * random, high-entropy, single-use, and never exposed in logs or
     * API responses.
     * </p>
     */
    @NotBlank(message = "Token is required")
    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    /**
     * The user this reset token belongs to.
     */
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The moment after which the token can no longer be used.
     */
    @NotNull(message = "Expiry is required")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Whether the token has already been consumed.
     * <p>
     * Once {@code true} the token is permanently invalid, preventing
     * replay attacks.
     * </p>
     */
    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = Boolean.FALSE;

    /**
     * Checks whether the token has passed its expiry window.
     *
     * @return {@code true} if the token is expired, {@code false} otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

}
