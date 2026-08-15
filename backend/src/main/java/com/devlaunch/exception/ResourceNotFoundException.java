package com.devlaunch.exception;

/**
 * Thrown when a requested resource (e.g. user, role) cannot be found in the system.
 * <p>
 * Translates to an HTTP 404 Not Found response via {@link GlobalExceptionHandler}.
 * </p>
 *
 * @author DevLaunch
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail describing the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

}
