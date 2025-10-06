package com.githubpopularity.service;

import com.githubpopularity.model.GithubRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for scoring GitHub repositories using a configurable scoring strategy.
 * Delegates the scoring logic to the injected {@link PopularityScoringStrategy}.
 */
@Service
public class PopularityScoringService {

    private final PopularityScoringStrategy scoringStrategy;

    public PopularityScoringService(PopularityScoringStrategy scoringStrategy) {
        this.scoringStrategy = scoringStrategy;
    }

    /**
     * Scores a list of repositories using the configured scoring strategy.
     *
     * @param repos list of {@link GithubRepository} to score
     * @return list of repositories with calculated popularity scores
     */
    public List<GithubRepository> scoreRepositories(List<GithubRepository> repos) {
        return scoringStrategy.calculateScores(repos);
    }
}
