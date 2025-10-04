package com.githubpopularity.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.githubpopularity.dto.GithubRepositoryItem;
import com.githubpopularity.model.GithubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for fetching repositories from the GitHub API.
 * Handles error cases and maps API responses to domain models.
 */
@Service
public class GithubRepositoryServiceImpl implements GithubRepositoryService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GithubRepositoryServiceImpl.class);
    private final WebClient webClient;

    @Autowired
    public GithubRepositoryServiceImpl(WebClient.Builder webClientBuilder,
                                       @Value("${github.api.base-url}") String githubApiBaseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(githubApiBaseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }

    @Override
    public List<GithubRepository> fetchRepositories(String language, String createdAfter, int perPage, int page) {
        String query = String.format("language:%s created:>%s", language, createdAfter);
        try {
            Mono<GithubSearchResponse> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/repositories")
                            .queryParam("q", query)
                            .queryParam("sort", "stars")
                            .queryParam("order", "desc")
                            .queryParam("per_page", perPage)
                            .queryParam("page", page)
                            .build())
                    .retrieve()
                    .bodyToMono(GithubSearchResponse.class)
                    .onErrorResume(WebClientResponseException.class, e -> {
                        handleWebClientError(e);
                        return Mono.empty();
                    });


            GithubSearchResponse response = responseMono.block();

            List<GithubRepositoryItem> items = Optional.ofNullable(response)
                    .map(r -> r.items)
                    .orElse(Collections.emptyList());

            return items.stream()
                    .map(this::mapToRepository)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching repositories from GitHub: {}", e.getMessage());
            return List.of();
        }
    }

    private void handleWebClientError(WebClientResponseException e) {
        int statusCode = e.getStatusCode().value();
        if (statusCode == 403 || statusCode == 429) {
            logger.error("GitHub API rate limit exceeded: {}", e.getMessage());
        } else {
            logger.error("GitHub API error ({}): {}", statusCode, e.getResponseBodyAsString());
        }
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
                0.0 // Initial score, to be calculated later
        );
    }

    private static class GithubSearchResponse {
        @JsonProperty("items")
        public List<GithubRepositoryItem> items;
    }
}
