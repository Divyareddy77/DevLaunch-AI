package com.devlaunch.service.interfaces;

import com.devlaunch.entity.User;

/**
 * Reusable email delivery abstraction for DevLaunch.
 * <p>
 * The concrete implementation is backed by Spring Boot Mail (SMTP) so it
 * can later be swapped for a provider such as SendGrid or Mailgun without
 * changing any calling code. All transactional email templates (currently
 * the password reset email) are sent through this interface.
 * </p>
 *
 * @author DevLaunch
 */
public interface EmailService {

    /**
     * Sends the password reset email to the given user.
     * <p>
     * The email contains a clickable one-time reset link built from the
     * configured frontend base URL and the given token, plus the expiry
     * window in minutes. Delivery failures are logged by the implementation
     * and never propagate, so the caller can always respond with the generic
     * "if an account exists" message without leaking state.
     * </p>
     *
     * @param user       the user requesting the reset (recipient + greeting name)
     * @param resetToken the opaque one-time reset token to embed in the link
     */
    void sendPasswordResetEmail(User user, String resetToken);

}
