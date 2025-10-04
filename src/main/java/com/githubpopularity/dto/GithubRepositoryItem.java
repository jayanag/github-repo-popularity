package com.githubpopularity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record GithubRepositoryItem(String name,
                                   @JsonProperty("full_name") String fullName,
                                   String description,
                                   @JsonProperty("stargazers_count") int stars,
                                   @JsonProperty("forks_count") int forks,
                                   @JsonProperty("updated_at") Instant lastUpdated,
                                   String language
) {
}