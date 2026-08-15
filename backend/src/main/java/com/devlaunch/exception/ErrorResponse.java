package com.devlaunch.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Standard error response body returned by the global exception handler.
 * <p>
 * Provides a consistent, structured format for all API error responses
 * including timestamp, HTTP status, error type, human-readable message,
 * and the request path that triggered the error.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;

    private Integer status;

    private String error;

    private String message;

    private String path;

}
