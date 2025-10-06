package com.githubpopularity.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.githubpopularity.dto.GithubRepositoryItem;
import com.githubpopularity.exception.GithubApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link GithubApiClient} that communicates with the GitHub public API.
 */
@Component
public class GithubApiClientImpl implements GithubApiClient {
    private static final Logger logger = LoggerFactory.getLogger(GithubApiClientImpl.class);

    private final WebClient webClient;

    public GithubApiClientImpl(WebClient githubWebClient) {
        this.webClient = githubWebClient;
    }

    @Override
    public List<GithubRepositoryItem> fetchRepositories(String query, int perPage, int page) {
        logger.debug("Fetching repositories from GitHub API: query='{}', perPage={}, page={}", query, perPage, page);
        try {
            GithubSearchResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/repositories")
                            .queryParam("q", query)
                            .queryParam("per_page", perPage)
                            .queryParam("page", page)
                            .build())
                    .retrieve()
                    .bodyToMono(GithubSearchResponse.class)
                    .onErrorMap(WebClientResponseException.class, this::handleWebClientError)
                    .block();

            return Optional.ofNullable(response)
                    .map(r -> r.items)
                    .orElse(Collections.emptyList());
        } catch (GithubApiException e) {
            logger.error("GitHub API error (status={}): {}", e.getStatusCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error calling GitHub API: {}", e.getMessage(), e);
            throw new GithubApiException(500, "Unexpected error calling GitHub API");
        }
    }

    private GithubApiException handleWebClientError(WebClientResponseException e) {
        int statusCode = e.getStatusCode().value();
        String message = switch (statusCode) {
            case 304 -> "GitHub API returned 304 Not Modified";
            case 403, 429 -> "GitHub API rate limit exceeded";
            case 422 -> "GitHub API validation error";
            case 503 -> "GitHub API service unavailable";
            default -> "GitHub API error";
        };
        return new GithubApiException(statusCode, message);
    }

    private static class GithubSearchResponse {
        @JsonProperty("items")
        public List<GithubRepositoryItem> items;
    }
}
