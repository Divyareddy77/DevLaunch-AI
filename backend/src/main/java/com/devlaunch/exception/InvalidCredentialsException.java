package com.devlaunch.exception;

/**
 * Thrown when authentication fails due to invalid email or password.
 * <p>
 * Translates to an HTTP 401 Unauthorized response via {@link GlobalExceptionHandler}.
 * </p>
 *
 * @author DevLaunch
 */
public class InvalidCredentialsException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail describing the failed authentication
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }

}
