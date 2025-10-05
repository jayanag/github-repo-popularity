package com.githubpopularity.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GithubApiException.class)
    public ResponseEntity<ApiError> handleGithubApiException(GithubApiException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatusCode()).body(new ApiError(
                ex.getStatusCode(),
                "GitHub API Error",
                request.getRequestURI(),
                Map.of("error", ex.getMessage())
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
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
