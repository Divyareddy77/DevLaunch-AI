package com.devlaunch.service.impl;

import com.devlaunch.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduled cleanup for expired password reset tokens.
 * <p>
 * Expired tokens are already rejected at use time by the expiry check, so
 * this job only prevents stale rows from accumulating indefinitely. It runs
 * once a day at 03:00 server time.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class PasswordResetTokenScheduler {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetTokenScheduler.class);

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Constructs the scheduler with the token repository.
     *
     * @param passwordResetTokenRepository repository for reset tokens
     */
    public PasswordResetTokenScheduler(final PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    /**
     * Deletes every token whose expiry has passed.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpiredTokens() {
        final long deleted = passwordResetTokenRepository.deleteExpiredBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Purged {} expired password reset tokens", deleted);
        }
    }

}
