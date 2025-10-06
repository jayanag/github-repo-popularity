package com.githubpopularity.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.githubpopularity.exception.GithubApiException;
import com.githubpopularity.model.GithubRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class GithubRepositoryServiceImplTest {

    private static WireMockServer wireMockServer;
    private GithubRepositoryServiceImpl service;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setup() {
        WebClient.Builder builder = WebClient.builder();
        service = new GithubRepositoryServiceImpl(builder, "http://localhost:8089");
    }

    @Test
    void fetchRepositoriesReturnsList() {
        stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", containing("language:Java"))
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
                                      "updated_at": "2025-10-05T00:00:00Z",
                                      "language": "Java"
                                    }
                                  ]
                                }
                                """)));

        List<GithubRepository> repos = service.fetchRepositories("Java", "2023-01-01", 10, 1);
        assertEquals(1, repos.size());
        GithubRepository repo = repos.get(0);
        assertEquals("repo1", repo.name());
        assertEquals("owner/repo1", repo.fullName());
        assertEquals("Java", repo.language());
        assertEquals(100, repo.stars());
        assertEquals(50, repo.forks());
        assertEquals("desc", repo.description());
    }

    @Test
    void fetchRepositories503throwsGithubApiException() {
        stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", containing("language:Go"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("Service Unavailable")));

        GithubApiException exception = assertThrows(GithubApiException.class, () ->
                service.fetchRepositories("Go", "2023-01-01", 10, 1));

        assertEquals(503, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("service unavailable"));
    }

    @Test
    void fetchRepositoriesEmptyResponseReturnsEmptyList() {
        stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", containing("language:Python"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"items\": [] }")));

        List<GithubRepository> repos = service.fetchRepositories("Python", "2023-01-01", 10, 1);
        assertTrue(repos.isEmpty());
    }
}
