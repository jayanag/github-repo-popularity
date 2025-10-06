package com.githubpopularity.service;

import com.githubpopularity.model.GithubRepository;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticThresholdStrategy implements PopularityScoringStrategy {
    private static final Logger logger = LoggerFactory.getLogger(StaticThresholdStrategy.class);

    private final int maxStars;
    private final int maxForks;
    private final int recencyDecayDays;

    public StaticThresholdStrategy(int maxStars, int maxForks, int recencyDecayDays) {
        this.maxStars = maxStars;
        this.maxForks = maxForks;
        this.recencyDecayDays = recencyDecayDays;
    }

    @Override
    public GithubRepository calculateScore(GithubRepository repo) {
        logger.debug("Calculating popularity score for repository: {}", repo.fullName());
        double starsScore = calculateNormalizedScore(repo.stars(), maxStars);
        double forksScore = calculateNormalizedScore(repo.forks(), maxForks);
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
        return Math.exp(-daysSinceUpdate / (double) recencyDecayDays);
    }
}