package com.githubpopularity.controller.integration;

import com.githubpopularity.model.GithubRepository;
import com.githubpopularity.GithubPopularityApplication;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = GithubPopularityApplication.class)
class RepositoryControllerIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private TestRestTemplate restTemplate;

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

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", () -> "http://localhost:8089");
    }

    @Test
    void testGetPopularRepositories200() {
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

        ResponseEntity<GithubRepository[]> response = restTemplate.getForEntity(
                "/api/repositories/popularity?language=Java&createdAfter=2023-01-01",
                GithubRepository[].class
        );

        assertEquals(200, response.getStatusCodeValue());
        GithubRepository[] repos = response.getBody();
        assertEquals(1, repos.length);
        assertEquals("repo1", repos[0].name());
    }

    @Test
    void testGetPopularRepositories204() {
        stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", containing("language:Python"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"items\": [] }")));

        ResponseEntity<Void> response = restTemplate.getForEntity(
                "/api/repositories/popularity?language=Python&createdAfter=2023-01-01",
                Void.class
        );

        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testGetPopularRepositories503() {
        stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", containing("language:Go"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("Service Unavailable")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/repositories/popularity?language=Go&createdAfter=2023-01-01",
                String.class
        );

        assertEquals(503, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("GitHub API service unavailable"));
    }

    @Test
    void testGetPopularRepositories400BlankLanguage() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/repositories/popularity?language=&createdAfter=2023-01-01",
                String.class
        );

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("must not be blank"));
    }

    @Test
    void testGetPopularRepositories422GithubValidationError() {
        stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", containing("language:Rust"))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withBody("Validation Failed")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/repositories/popularity?language=Rust&createdAfter=2023-01-01",
                String.class
        );

        assertEquals(422, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("GitHub API validation error"));
    }

    @Test
    void testGetPopularRepositories403RateLimitExceeded() {
        stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", containing("language:Ruby"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withBody("API rate limit exceeded")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/repositories/popularity?language=Ruby&createdAfter=2023-01-01",
                String.class
        );

        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("rate limit exceeded"));
    }

    @Test
    void testGetPopularRepositories500UnexpectedError() {
        stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", containing("language:C"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal GitHub Error")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/repositories/popularity?language=C&createdAfter=2023-01-01",
                String.class
        );

        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("GitHub API error"));
    }
}