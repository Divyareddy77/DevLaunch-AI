package com.devlaunch.exception;

/**
 * Thrown when a password reset token has passed its expiry window.
 *
 * @author DevLaunch
 */
public class PasswordResetTokenExpiredException extends RuntimeException {

    /**
     * Constructs the exception with a detail message.
     *
     * @param message the detail message
     */
    public PasswordResetTokenExpiredException(String message) {
        super(message);
    }

}
