package com.githubpopularity.controller;

import com.githubpopularity.model.GithubRepository;
import com.githubpopularity.service.GithubRepositoryService;
import com.githubpopularity.service.PopularityScoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepositoryControllerTest {

    private GithubRepositoryService githubService;
    private PopularityScoringService scoringService;
    private RepositoryController controller;

    @BeforeEach
    void setUp() {
        githubService = mock(GithubRepositoryService.class);
        scoringService = mock(PopularityScoringService.class);
        controller = new RepositoryController(githubService, scoringService);
    }

    @Test
    void testGetPopularRepositoriesReturns200() {
        GithubRepository repo = new GithubRepository(
                "repo1", "owner/repo1", "desc", 100, 50,
                Instant.parse("2025-10-05T00:00:00Z"), "Java", 0.0
        );

        when(githubService.fetchRepositories("Java", "2023-01-01", 10, 1))
                .thenReturn(List.of(repo));
        when(scoringService.scoreRepositories(List.of(repo)))
                .thenReturn(List.of(repo.withScore(75.0)));

        ResponseEntity<List<GithubRepository>> response = controller.getPopularRepositories(
                "Java", "2023-01-01", 10, 1
        );

        assertEquals(200, response.getStatusCodeValue());
        List<GithubRepository> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("repo1", body.get(0).name());

        verify(githubService, times(1)).fetchRepositories("Java", "2023-01-01", 10, 1);
        verify(scoringService, times(1)).scoreRepositories(List.of(repo));
    }

    @Test
    void testGetPopularRepositoriesReturns204WhenNoRepos() {
        when(githubService.fetchRepositories("Python", "2023-01-01", 10, 1))
                .thenReturn(List.of());
        when(scoringService.scoreRepositories(List.of())).thenReturn(List.of());

        ResponseEntity<List<GithubRepository>> response = controller.getPopularRepositories(
                "Python", "2023-01-01", 10, 1
        );

        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());

        verify(githubService, times(1)).fetchRepositories("Python", "2023-01-01", 10, 1);
        verify(scoringService, times(1)).scoreRepositories(List.of());
    }
}
