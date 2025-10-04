package com.githubpopularity.model;

import java.time.Instant;

/**
 * Domain model representing a GitHub repository with popularity score.
 * Immutable and used throughout the application.
 */
public record GithubRepository(String name,
                               String fullName,
                               String description,
                               int stars,
                               int forks,
                               Instant lastUpdated,
                               String language,
                               double popularityScore) {
    /**
     * Returns a new GithubRepository instance with the given popularity score.
     *
     * @param score the new popularity score
     * @return a new GithubRepository with the updated score
     */
    public GithubRepository withScore(double score) {
        return new GithubRepository(name, fullName, description, stars, forks, lastUpdated, language, score);
    }
}
