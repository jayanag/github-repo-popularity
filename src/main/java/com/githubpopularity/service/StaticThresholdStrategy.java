package com.githubpopularity.service;

import com.githubpopularity.model.GithubRepository;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticThresholdStrategy implements PopularityScoringStrategy {
    private static final int MAX_STARS = 50000;
    private static final int MAX_FORKS = 10000;
    private static final int RECENCY_DECAY_DAYS = 3;
    private static final Logger logger = LoggerFactory.getLogger(StaticThresholdStrategy.class);

    @Override
    public GithubRepository calculateScore(GithubRepository repo) {
        logger.debug("Calculating popularity score for repository: {}", repo.fullName());
        double starsScore = calculateNormalizedScore(repo.stars(), MAX_STARS);
        double forksScore = calculateNormalizedScore(repo.forks(), MAX_FORKS);
        double recencyScore = calculateRecencyScore(repo.lastUpdated());

        double score = (starsScore * 0.6 + forksScore * 0.3 + recencyScore * 0.1) * 100;
        logger.debug("Score details - stars: {}, forks: {}, recency: {}, final: {}", starsScore, forksScore, recencyScore, score);
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