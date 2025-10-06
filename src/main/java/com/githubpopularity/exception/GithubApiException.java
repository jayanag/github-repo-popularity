package com.githubpopularity.exception;

/**
 * Custom runtime exception to represent errors returned from the GitHub API.
 * Encapsulates the HTTP status code and the error message.
 */
public class GithubApiException extends RuntimeException {
    private final int statusCode;

    public GithubApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
