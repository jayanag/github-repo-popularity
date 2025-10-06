package com.githubpopularity.service;

import com.githubpopularity.client.GithubApiClient;
import com.githubpopularity.dto.GithubRepositoryItem;
import com.githubpopularity.model.GithubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GithubRepositoryServiceImplTest {

    private GithubApiClient githubApiClient;
    private GithubRepositoryServiceImpl repositoryService;

    @BeforeEach
    void setUp() {
        githubApiClient = mock(GithubApiClient.class);
        repositoryService = new GithubRepositoryServiceImpl(githubApiClient);
    }

    @Test
    void fetchRepositoriesReturnsMappedRepositories() {
        GithubRepositoryItem item = new GithubRepositoryItem(
                "repo1",
                "owner/repo1",
                "A test repo",
                100,
                50,
                Instant.parse("2025-10-05T00:00:00Z"),
                "Java"
        );

        when(githubApiClient.fetchRepositories(anyString(), eq(10), eq(1)))
                .thenReturn(List.of(item));

        List<GithubRepository> result = repositoryService.fetchRepositories("Java", "2023-01-01", 10, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("repo1", result.get(0).name());
        assertEquals("owner/repo1", result.get(0).fullName());

        verify(githubApiClient, times(1)).fetchRepositories(anyString(), eq(10), eq(1));
    }

    @Test
    void fetchRepositoriesEmptyListReturnsEmpty() {
        when(githubApiClient.fetchRepositories(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of());

        List<GithubRepository> result = repositoryService.fetchRepositories("Python", "2023-01-01", 5, 2);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(githubApiClient, times(1)).fetchRepositories(anyString(), eq(5), eq(2));
    }

    @Test
    void fetchRepositoriesClientThrowsException() {
        when(githubApiClient.fetchRepositories(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("API error"));

        assertThrows(RuntimeException.class,
                () -> repositoryService.fetchRepositories("Go", "2023-01-01", 5, 1));

        verify(githubApiClient, times(1)).fetchRepositories(anyString(), eq(5), eq(1));
    }
}
