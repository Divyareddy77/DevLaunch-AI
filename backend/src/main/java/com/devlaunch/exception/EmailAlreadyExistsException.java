package com.devlaunch.exception;

/**
 * Thrown when a user attempts to register with an email address
 * that is already associated with an existing account.
 * <p>
 * Translates to an HTTP 409 Conflict response via {@link GlobalExceptionHandler}.
 * </p>
 *
 * @author DevLaunch
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail describing the duplicate email conflict
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }

}

