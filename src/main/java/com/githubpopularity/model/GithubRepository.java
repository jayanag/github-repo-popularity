package com.githubpopularity.model;

import java.time.Instant;

public record GithubRepository(String name,
                               String fullName,
                               String htmlUrl,
                               String description,
                               int stars,
                               int forks,
                               Instant lastUpdated,
                               String language,
                               double popularityScore) {
}
