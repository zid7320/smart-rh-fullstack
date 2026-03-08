package com.smart.rh.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Centralised error envelope returned for ALL error responses.
 *
 * <pre>
 * Standard (404 / 400 explicit / 500):
 * {
 *   "timestamp":  "2024-01-15T10:30:00Z",
 *   "status":     404,
 *   "error":      "Not Found",
 *   "message":    "Employe not found with id : '99'",
 *   "path":       "/api/employees/99"
 * }
 *
 * Bean-validation (400) — fieldErrors is populated:
 * {
 *   "timestamp":  "2024-01-15T10:30:00Z",
 *   "status":     400,
 *   "error":      "Validation Failed",
 *   "message":    "Request validation failed",
 *   "path":       "/api/leaves",
 *   "fieldErrors": { "employeId": "must not be null" }
 * }
 * </pre>
 */
@Getter
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
                timezone = "UTC")
    private final Instant timestamp = Instant.now();

    private final int    status;
    private final String error;
    private final String message;
    private final String path;

    /**
     * Field-level validation errors keyed by field name.
     * Omitted (not serialised) when null.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Map<String, String> fieldErrors;

    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, null);
    }

    public ErrorResponse(int status, String error, String message,
                         String path, Map<String, String> fieldErrors) {
        this.status      = status;
        this.error       = error;
        this.message     = message;
        this.path        = path;
        this.fieldErrors = fieldErrors;
    }
}
