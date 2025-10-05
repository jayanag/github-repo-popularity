package com.githubpopularity.controller;

import com.githubpopularity.model.GithubRepository;
import com.githubpopularity.service.GithubRepositoryService;
import com.githubpopularity.service.PopularityScoringService;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@Validated
public class RepositoryController {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryController.class);
    private final GithubRepositoryService githubService;
    private final PopularityScoringService scoringService;

    public RepositoryController(GithubRepositoryService githubService, PopularityScoringService scoringService) {
        this.githubService = githubService;
        this.scoringService = scoringService;
    }

    @GetMapping("/popularity")
    public ResponseEntity<List<GithubRepository>> getPopularRepositories(
            @RequestParam @NotBlank String language,
            @RequestParam @NotBlank @Pattern(
                    regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                    message = "createdAfter must be in the format YYYY-MM-DD"
            ) String createdAfter,
            @RequestParam(defaultValue = "10") @Min(1) int perPage,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        logger.info("Fetching repositories for language={}, createdAfter={}, perPage={}, page={}",
                language, createdAfter, perPage, page);

        List<GithubRepository> repositories = githubService.fetchRepositories(language, createdAfter, perPage, page);
        List<GithubRepository> scored = scoringService.scoreRepositories(repositories);

        return scored.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(scored);
    }
}