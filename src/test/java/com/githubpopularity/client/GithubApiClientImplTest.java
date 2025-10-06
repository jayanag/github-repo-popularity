package com.githubpopularity.client;

import com.githubpopularity.configuration.WebClientConfig;
import com.githubpopularity.dto.GithubRepositoryItem;
import com.githubpopularity.exception.GithubApiException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class GithubApiClientImplTest {

    private static WireMockServer wireMockServer;
    private static GithubApiClientImpl githubApiClient;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        configureFor("localhost", 8089);

        String baseUrl = wireMockServer.baseUrl();
        WebClientConfig config = new WebClientConfig();
        WebClient webClient = config.githubWebClient(WebClient.builder(), baseUrl);
        githubApiClient = new GithubApiClientImpl(webClient);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setup() {
        wireMockServer.resetAll();
    }

    @Test
    void testFetchRepositoriesReturnsList() {
        stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", containing("language:Java"))
                .withQueryParam("per_page", equalTo("10"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "items": [
                                    {
                                      "name": "repo1",
                                      "full_name": "owner/repo1",
                                      "description": "desc",
                                      "stargazers_count": 100,
                                      "forks_count": 50,
                                      "updated_at": "2025-10-06T00:00:00Z",
                                      "language": "Java"
                                    }
                                  ]
                                }
                                """)));

        List<GithubRepositoryItem> items = githubApiClient.fetchRepositories("language:Java", 10, 1);

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("repo1", items.get(0).name());
    }

    @Test
    void testFetchRepositoriesEmptyResponseReturnsEmptyList() {
        stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", containing("language:Python"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"items\": [] }")));

        List<GithubRepositoryItem> items = githubApiClient.fetchRepositories("language:Python", 5, 2);
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void testFetchRepositoriesRateLimitThrowsException() {
        stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", containing("language:Go"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withBody("API rate limit exceeded")));

        GithubApiException ex = assertThrows(GithubApiException.class,
                () -> githubApiClient.fetchRepositories("language:Go", 10, 1));

        assertEquals(403, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("rate limit exceeded"));
    }

    @Test
    void testFetchRepositoriesServiceUnavailableThrowsException() {
        stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", containing("language:Rust"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("Service Unavailable")));

        GithubApiException ex = assertThrows(GithubApiException.class,
                () -> githubApiClient.fetchRepositories("language:Rust", 10, 1));

        assertEquals(503, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("service unavailable"));
    }
}
