package com.example.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for the HRMS application.
 * 
 * This class serves as a centralized point for handling exceptions across all controllers
 * in the application. It intercepts exceptions and converts them into user-friendly,
 * structured JSON error responses.
 * 
 * Key responsibilities:
 * - Converts {@link MethodArgumentNotValidException} (request body validation failures) 
 *   into a clean JSON map of field names and their validation error messages
 * - Converts {@link ConstraintViolationException} (path variable/parameter validation failures)
 *   into a clean JSON map of constraint violations
 * - Handles unexpected generic exceptions gracefully
 * - Returns appropriate HTTP status codes (400 for validation errors, 500 for server errors)
 * 
 * Response format for validation errors (400 Bad Request):
 * <pre>
 * {
 *   "nom": "Nom is mandatory",
 *   "email": "Email must be valid",
 *   "salaire": "Amount must be a positive number"
 * }
 * </pre>
 * 
 * This handler is automatically applied to all REST controllers in the application
 * through Spring's @ControllerAdvice mechanism, ensuring consistent error handling
 * across all endpoints.
 * 
 * @author HRMS Development Team
 * @version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation exceptions that occur when @Valid annotation fails
     * on @RequestBody parameters.
     * 
     * This method is triggered when Spring Data Binding encounters validation errors
     * during request body deserialization and validation. It extracts all field errors
     * and returns them as a structured JSON map.
     * 
     * @param ex the MethodArgumentNotValidException containing field validation errors
     * @param request the current web request
     * @return a ResponseEntity containing a map of field names to error messages,
     *         with HTTP status 400 (Bad Request)
     * 
     * Example response:
     * <pre>
     * {
     *   "nom": "Nom is mandatory",
     *   "email": "Email must be valid"
     * }
     * </pre>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        
        // Extract all field errors from the binding result
        ex.getBindingResult()
            .getFieldErrors()
            .forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
        
        // Also handle global errors (object-level validation errors)
        ex.getBindingResult()
            .getGlobalErrors()
            .forEach(error -> 
                errors.put(error.getObjectName(), error.getDefaultMessage())
            );
        
        return ResponseEntity
            .badRequest()
            .body(errors);
    }

    /**
     * Handles constraint violation exceptions that occur during path variable
     * or parameter validation.
     * 
     * This method is triggered when @Validated or @Valid annotations on method parameters
     * (not request body) encounter violations. It collects all violations and returns them
     * as a structured JSON map.
     * 
     * @param ex the ConstraintViolationException containing constraint violations
     * @param request the current web request
     * @return a ResponseEntity containing a map of property paths to violation messages,
     *         with HTTP status 400 (Bad Request)
     * 
     * Example response:
     * <pre>
     * {
     *   "updateEmployee.id": "must be greater than 0",
     *   "updateEmployee.email": "must be a valid email address"
     * }
     * </pre>
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        
        // Extract all constraint violations
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        }
        
        return ResponseEntity
            .badRequest()
            .body(errors);
    }

    /**
     * Handles constraint violation exceptions that occur during entity persistence
     * (Hibernate validation at save/update time).
     * 
     * This method is triggered when Hibernate encounters validation errors while
     * persisting an entity to the database. It extracts constraint violations from the
     * wrapped exception and returns them as a structured JSON map.
     * 
     * This typically happens when:
     * - Entity-level @NotNull, @NotBlank constraints are violated
     * - Database-level constraints are violated
     * - Custom validation rules on the entity fail
     * 
     * @param ex the PersistenceException containing Hibernate validation errors
     * @param request the current web request
     * @return a ResponseEntity containing a map of field names to violation messages,
     *         with HTTP status 400 (Bad Request)
     * 
     * Example response:
     * <pre>
     * {
     *   "nom": "Nom is mandatory",
     *   "email": "Email must be valid",
     *   "poste": "Poste is mandatory"
     * }
     * </pre>
     */
    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<Map<String, String>> handlePersistenceException(
            PersistenceException ex,
            WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        
        // Check if the cause is a ConstraintViolationException
        Throwable cause = ex.getCause();
        if (cause instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) cause;
            
            // Extract all constraint violations from Hibernate
            for (ConstraintViolation<?> violation : cve.getConstraintViolations()) {
                String propertyPath = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                errors.put(propertyPath, message);
            }
        } else {
            // If not a constraint violation, provide the raw message
            errors.put("error", ex.getMessage() != null ? ex.getMessage() : "Database operation failed");
        }
        
        return ResponseEntity
            .badRequest()
            .body(errors);
    }

    /**
     * Handles all other unexpected exceptions that are not specifically handled
     * by other exception handlers.
     * 
     * This is a fallback handler that ensures no exception goes unhandled, providing
     * a consistent error response format for unexpected server-side errors.
     * 
     * @param ex the generic Exception
     * @param request the current web request
     * @return a ResponseEntity containing error details with HTTP status 500
     *         (Internal Server Error)
     * 
     * Example response:
     * <pre>
     * {
     *   "error": "Unexpected error",
     *   "message": "Database connection failed",
     *   "status": "500"
     * }
     * </pre>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(
            Exception ex,
            WebRequest request) {
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "Unexpected error");
        error.put("message", ex.getMessage() != null ? ex.getMessage() : "An unknown error occurred");
        error.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }
}
