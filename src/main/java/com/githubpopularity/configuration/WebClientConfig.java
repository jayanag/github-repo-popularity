package com.githubpopularity.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient used to communicate with the GitHub API.
 */
@Configuration
public class WebClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);

    @Bean
    public WebClient githubWebClient(
            WebClient.Builder webClientBuilder,
            @Value("${github.api.base-url}") String githubApiBaseUrl) {

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB buffer
                .build();

        logger.info("Configured WebClient bean for GitHub API with base URL: {}", githubApiBaseUrl);

        return webClientBuilder
                .baseUrl(githubApiBaseUrl)
                .exchangeStrategies(strategies)
                .build();
    }
}
