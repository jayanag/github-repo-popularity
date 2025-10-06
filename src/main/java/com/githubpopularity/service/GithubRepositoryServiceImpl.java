package com.githubpopularity.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.githubpopularity.client.GithubApiClient;
import com.githubpopularity.dto.GithubRepositoryItem;
import com.githubpopularity.exception.GithubApiException;
import com.githubpopularity.model.GithubRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for fetching GitHub repositories using the public API.
 * Converts API responses into domain model {@link GithubRepository} objects.
 */
@Service
public class GithubRepositoryServiceImpl implements GithubRepositoryService {
    private static final Logger logger = LoggerFactory.getLogger(GithubRepositoryServiceImpl.class);
    private final GithubApiClient githubApiClient;

    public GithubRepositoryServiceImpl(GithubApiClient githubApiClient) {
        this.githubApiClient = githubApiClient;
    }

    /**
     * Fetches repositories from GitHub filtered by language and creation date, with paging.
     *
     * @param language     programming language to filter repositories
     * @param createdAfter only repositories created after this date (YYYY-MM-DD)
     * @param perPage      number of repositories per page
     * @param page         page number
     * @return list of {@link GithubRepository} objects; empty if none found
     * @throws GithubApiException if GitHub API returns an error or unexpected exception occurs
     */
    @Override
    public List<GithubRepository> fetchRepositories(String language, String createdAfter, int perPage, int page) {
        String query = String.format("language:%s created:>%s", language, createdAfter);
        logger.debug("Fetching repositories with query='{}'", query);

        List<GithubRepositoryItem> items = githubApiClient.fetchRepositories(query, perPage, page);

        if (items.isEmpty()) {
            logger.warn("No repositories returned from GitHub for query='{}'", query);
            return List.of();
        }

        logger.info("Mapping {} repositories into domain model", items.size());

        return items.stream()
                .map(this::mapToRepository)
                .collect(Collectors.toList());
    }

    private GithubRepository mapToRepository(GithubRepositoryItem item) {
        return new GithubRepository(
                item.name(),
                item.fullName(),
                item.description(),
                item.stars(),
                item.forks(),
                item.lastUpdated(),
                item.language(),
                0.0
        );
    }

    private static class GithubSearchResponse {
        @JsonProperty("items")
        public List<GithubRepositoryItem> items;
    }
}
