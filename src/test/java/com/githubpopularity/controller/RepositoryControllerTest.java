package com.githubpopularity.controller;

import com.githubpopularity.model.GithubRepository;
import com.githubpopularity.service.GithubRepositoryService;
import com.githubpopularity.service.PopularityScoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(RepositoryController.class)
class RepositoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GithubRepositoryService githubRepositoryService;

    @MockBean
    private PopularityScoringService popularityScoringService;

    private GithubRepository sampleRepo;

    @BeforeEach
    void setUp() {
        sampleRepo = new GithubRepository(
                "repo1",
                "user/repo1",
                "A test repository",
                1500,
                200,
                Instant.now(),
                "Java",
                85.5
        );
    }

    @Test
    void testGetPopularRepositories_ReturnsRepositories() throws Exception {
        // Mock the service responses
        Mockito.when(githubRepositoryService.fetchRepositories(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(sampleRepo));

        Mockito.when(popularityScoringService.scoreRepositories(anyList()))
                .thenReturn(List.of(sampleRepo));

        // Perform GET request
        mockMvc.perform(get("/api/repositories/popularity")
                        .param("language", "Java")
                        .param("createdAfter", "2024-01-01")
                        .param("perPage", "5")
                        .param("page", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("repo1"))
                .andExpect(jsonPath("$[0].fullName").value("user/repo1"))
                .andExpect(jsonPath("$[0].description").value("A test repository"))
                .andExpect(jsonPath("$[0].stars").value(1500))
                .andExpect(jsonPath("$[0].forks").value(200))
                .andExpect(jsonPath("$[0].language").value("Java"))
                .andExpect(jsonPath("$[0].popularityScore").value(85.5));

        // Verify interactions
        Mockito.verify(githubRepositoryService).fetchRepositories(eq("Java"), eq("2024-01-01"), eq(5), eq(1));
        Mockito.verify(popularityScoringService).scoreRepositories(anyList());
    }

    @Test
    void testGetPopularRepositories_EmptyList() throws Exception {
        Mockito.when(githubRepositoryService.fetchRepositories(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of());
        Mockito.when(popularityScoringService.scoreRepositories(anyList()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/repositories/popularity")
                        .param("language", "Python")
                        .param("createdAfter", "2024-05-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        }
}

