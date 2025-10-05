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
        double starsScore = calculateNormalizedScore(repo.stars(), MAX_STARS);
        double forksScore = calculateNormalizedScore(repo.forks(), MAX_FORKS);
        double recencyScore = calculateRecencyScore(repo.lastUpdated());

        double score = (starsScore * 0.6 + forksScore * 0.3 + recencyScore * 0.1) * 100;
        return repo.withScore(score);
    }

    private double calculateNormalizedScore(int value, int max) {
        return Math.min(1.0, (double) value / max);
    }

    private double calculateRecencyScore(Instant lastUpdated) {
        long daysSinceUpdate = Duration.between(lastUpdated, Instant.now()).toDays();
        return Math.exp(-daysSinceUpdate / (double) RECENCY_DECAY_DAYS);
    }
}