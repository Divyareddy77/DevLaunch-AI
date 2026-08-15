package com.devlaunch.messaging.event;

/**
 * Lightweight event requesting the delivery of a password reset email.
 * <p>
 * Published by the authentication service instead of calling the
 * {@code EmailService} directly. The consumer resolves the recipient user
 * and delegates to the existing email module, which owns the message
 * template. Only the user ID and the one-time reset token travel on the
 * wire — the token is never logged anywhere.
 * </p>
 *
 * @param userId      the ID of the user requesting the reset
 * @param resetToken  the opaque one-time reset token to embed in the link
 * @author DevLaunch
 */
public record ForgotPasswordEmailEvent(Long userId, String resetToken) {
}
