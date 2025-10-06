package com.githubpopularity.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GithubApiException.class)
    public ResponseEntity<ApiError> handleGithubApiException(GithubApiException ex, HttpServletRequest request) {
        logger.error("GitHubApiException: status={}, message={}", ex.getStatusCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(new ApiError(
                ex.getStatusCode(),
                "GitHub API Error",
                request.getRequestURI(),
                Map.of("error", ex.getMessage())
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        logger.warn("ConstraintViolationException: {}", ex.getMessage());
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage()
                ));
        return ResponseEntity.badRequest().body(new ApiError(
                400,
                "Validation Error",
                request.getRequestURI(),
                errors
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        ApiError apiError = new ApiError(
                500,
                "Internal Server Error",
                request.getRequestURI(),
                Map.of("error", ex.getMessage())
        );
        return ResponseEntity.status(500).body(apiError);
    }

    public static class ApiError {
        private final Instant timestamp = Instant.now();
        private final int status;
        private final String error;
        private final String path;
        private final Map<String, String> errors;

        public ApiError(int status, String error, String path, Map<String, String> errors) {
            this.status = status;
            this.error = error;
            this.path = path;
            this.errors = errors;
        }

        public Instant getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getPath() { return path; }
        public Map<String, String> getErrors() { return errors; }
    }
}
