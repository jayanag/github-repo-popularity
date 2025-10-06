package com.githubpopularity.service;

import com.githubpopularity.model.GithubRepository;
import java.util.List;

/**
 * Strategy interface for calculating popularity scores for GitHub repositories.
 * Implementations define how a repository's score is computed.
 */
public interface PopularityScoringStrategy {
    /**
     * Calculate the popularity score for a single repository.
     *
     * @param repository the repository to score
     * @return the repository with its popularity score set
     */
    GithubRepository calculateScore(GithubRepository repository);

    /**
     * Calculate popularity scores for a list of repositories.
     * Uses {@link #calculateScore(GithubRepository)} for each repository.
     *
     * @param repos list of repositories to score
     * @return list of scored repositories
     */
    default List<GithubRepository> calculateScores(List<GithubRepository> repos) {
        return repos.stream()
                .map(this::calculateScore)
                .toList();
    }
}