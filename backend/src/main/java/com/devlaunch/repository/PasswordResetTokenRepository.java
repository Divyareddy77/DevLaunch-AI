package com.devlaunch.repository;

import com.devlaunch.entity.PasswordResetToken;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for {@link PasswordResetToken} entity operations.
 * <p>
 * Provides lookup by the opaque token value and bulk deletion of a user's
 * outstanding (unused) tokens, which keeps every user to at most one
 * valid reset link at a time.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Finds a password reset token by its opaque token value.
     *
     * @param token the token value from the reset link
     * @return an {@link Optional} containing the token if found, or empty otherwise
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Deletes every unused token belonging to the given user.
     * <p>
     * Called before issuing a new reset token (so a previous link stops
     * working) and after a successful reset (so no other link for the
     * same user remains usable).
     * </p>
     *
     * @param user the user whose unused tokens to remove
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.user = :user AND t.used = false")
    void deleteUnusedTokens(@Param("user") User user);

    /**
     * Atomically marks a single token as used, but only if it is not
     * already used.
     * <p>
     * Returns the number of rows updated (1 on success, 0 if the token
     * was already consumed by a concurrent request), which closes the
     * check-then-set race on the single-use guarantee.
     * </p>
     *
     * @param token the token value to consume
     * @return 1 if the token was consumed by this call, 0 otherwise
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.token = :token AND t.used = false")
    int markUsedIfUnused(@Param("token") String token);

    /**
     * Deletes every token that expired before the given moment.
     * <p>
     * Used by the daily cleanup scheduler to stop expired rows from
     * accumulating.
     * </p>
     *
     * @param now the cutoff timestamp
     * @return the number of expired tokens removed
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    long deleteExpiredBefore(@Param("now") LocalDateTime now);

}
