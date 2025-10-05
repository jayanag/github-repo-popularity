package com.githubpopularity.service;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GithubRepositoryServiceImpl implements GithubRepositoryService {
    private static final Logger logger = LoggerFactory.getLogger(GithubRepositoryServiceImpl.class);
    private final WebClient webClient;

    public GithubRepositoryServiceImpl(WebClient.Builder webClientBuilder,
                                       @Value("${github.api.base-url}") String githubApiBaseUrl) {
        // Increase buffer size to 2MB to handle large GitHub API responses
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
        this.webClient = webClientBuilder
                .baseUrl(githubApiBaseUrl)
                .exchangeStrategies(strategies)
                .build();
    }

    @Override
    public List<GithubRepository> fetchRepositories(String language, String createdAfter, int perPage, int page) {
        String query = String.format("language:%s created:>%s", language, createdAfter);
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
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(this::mapToRepository)
                    .collect(Collectors.toList());
        } catch (GithubApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error fetching repositories: {}", e.getMessage(), e);
            throw new GithubApiException(500, "Unexpected error fetching repositories");
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
        logger.error("{}: {}", message, e.getMessage());
        return new GithubApiException(statusCode, message);
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
