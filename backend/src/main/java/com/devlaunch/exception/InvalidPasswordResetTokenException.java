package com.devlaunch.exception;

/**
 * Thrown when a password reset token does not exist or has already
 * been used.
 * <p>
 * The same message is returned for both cases so attackers cannot
 * distinguish a fabricated token from a consumed one.
 * </p>
 *
 * @author DevLaunch
 */
public class InvalidPasswordResetTokenException extends RuntimeException {

    /**
     * Constructs the exception with a detail message.
     *
     * @param message the detail message
     */
    public InvalidPasswordResetTokenException(String message) {
        super(message);
    }

}
