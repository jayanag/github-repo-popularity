package com.githubpopularity.controller;

import com.githubpopularity.model.GithubRepository;
import com.githubpopularity.service.GithubRepositoryService;
import com.githubpopularity.service.PopularityScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for fetching and scoring GitHub repositories by language and creation date.
 * Provides an endpoint to retrieve repositories with popularity scores.
 */
@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RepositoryController.class);
    private final GithubRepositoryService githubService;
    private final PopularityScoringService scoringService;


    public RepositoryController(GithubRepositoryService githubService, PopularityScoringService scoringService) {
        this.githubService = githubService;
        this.scoringService = scoringService;
    }


    @GetMapping("/popularity")
    public ResponseEntity<List<GithubRepository>> getPopularRepositories(
            @RequestParam String language,
            @RequestParam String createdAfter,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "1") int page) {

        logger.info("Fetching repositories for language={} createdAfter={}", language, createdAfter);

        List<GithubRepository> repositories = githubService.fetchRepositories(language, createdAfter, perPage, page);
        List<GithubRepository> scored = scoringService.scoreRepositories(repositories);

        if (scored.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(scored);
    }
}
