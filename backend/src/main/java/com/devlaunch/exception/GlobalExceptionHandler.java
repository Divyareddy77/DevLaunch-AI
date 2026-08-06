package com.devlaunch.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler for the DevLaunch API.
 * <p>
 * Intercepts exceptions thrown by controllers and returns consistent
 * {@link ErrorResponse} bodies with appropriate HTTP status codes.
 * Handles custom domain exceptions, validation failures, and
 * unexpected server errors.
 * </p>
 *
 * @author DevLaunch
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link EmailAlreadyExistsException} thrown during registration
     * when a user attempts to register with an already-used email.
     *
     * @param ex      the exception instance
     * @param request the current HTTP request
     * @return a 409 Conflict response with error details
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handles {@link ResourceNotFoundException} when a requested entity
     * (e.g. user, role) does not exist.
     *
     * @param ex      the exception instance
     * @param request the current HTTP request
     * @return a 404 Not Found response with error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handles {@link InvalidCredentialsException} thrown during authentication
     * when the provided email or password is incorrect.
     *
     * @param ex      the exception instance
     * @param request the current HTTP request
     * @return a 401 Unauthorized response with error details
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handles {@link AiTranscriptionException} thrown when speech-to-text
     * transcription fails upstream (provider error, network timeout, or an
     * audio payload the provider rejected).
     *
     * @param ex      the exception instance
     * @param request the current HTTP request
     * @return a 502 Bad Gateway response with error details
     */
    @ExceptionHandler(AiTranscriptionException.class)
    public ResponseEntity<ErrorResponse> handleAiTranscription(
            AiTranscriptionException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                "Bad Gateway",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handles {@link InvalidPasswordResetTokenException} thrown when a
     * password reset token does not exist or has already been used.
     *
     * @param ex      the exception instance
     * @param request the current HTTP request
     * @return a 400 Bad Request response with error details
     */
    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordResetToken(
            InvalidPasswordResetTokenException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handles {@link PasswordResetTokenExpiredException} thrown when a
     * password reset token has passed its expiry window.
     *
     * @param ex      the exception instance
     * @param request the current HTTP request
     * @return a 400 Bad Request response with error details
     */
    @ExceptionHandler(PasswordResetTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handlePasswordResetTokenExpired(
            PasswordResetTokenExpiredException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handles {@link IllegalArgumentException} thrown when a service method
     * receives an invalid argument, such as an incorrect current password
     * during a password change operation.
     *
     * @param ex      the exception instance
     * @param request the current HTTP request
     * @return a 400 Bad Request response with error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handles {@link MethodArgumentNotValidException} thrown when
     * {@code @Valid} validation on a request body fails.
     * <p>
     * Collects all field errors and returns the first validation message
     * in the response for clarity.
     * </p>
     *
     * @param ex      the exception containing validation errors
     * @param request the current HTTP request
     * @return a 400 Bad Request response with the first validation message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String firstErrorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .filter(message -> message != null)
                .findFirst()
                .orElse("Validation failed");

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                firstErrorMessage,
                request
        );
    }

    /**
     * Handles any unhandled exceptions as a fallback.
     * <p>
     * Returns a generic 500 Internal Server Error response to avoid
     * leaking sensitive information to the client.
     * </p>
     *
     * @param ex      the unexpected exception
     * @param request the current HTTP request
     * @return a 500 Internal Server Error response with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request
        );
    }

    /**
     * Constructs a standardized {@link ErrorResponse} and wraps it in a
     * {@link ResponseEntity} with the given HTTP status.
     *
     * @param status  the HTTP status to return
     * @param error   a short error type description
     * @param message a human-readable error message
     * @param request the current HTTP request (used to extract the URI)
     * @return a populated response entity
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status)
                .body(errorResponse);
    }

}
