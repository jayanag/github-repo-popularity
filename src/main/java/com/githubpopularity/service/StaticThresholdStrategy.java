package com.githubpopularity.service;

import com.githubpopularity.model.GithubRepository;

import java.time.Duration;
import java.time.Instant;

public class StaticThresholdStrategy implements PopularityScoringStrategy {
    private static final int MAX_STARS = 50000;
    private static final int MAX_FORKS = 10000;
    private static final int RECENCY_DECAY_DAYS = 3;

    @Override
    public GithubRepository calculateScore(GithubRepository repo) {
        double starsScore = Math.min(1.0, (double) repo.stars() / MAX_STARS);
        double forksScore = Math.min(1.0, (double) repo.forks() / MAX_FORKS);
        double recencyScore = Math.exp(-Duration.between(repo.lastUpdated(), Instant.now()).toDays() / (double) RECENCY_DECAY_DAYS);

        double score = (starsScore * 0.6 + forksScore * 0.3 + recencyScore * 0.1) * 100;

        return repo.withScore(score);
    }
}
