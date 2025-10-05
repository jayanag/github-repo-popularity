package com.githubpopularity.service;

import com.githubpopularity.model.GithubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class StaticThresholdStrategyTest {

    private StaticThresholdStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new StaticThresholdStrategy();
    }

    @Test
    void testCalculateScoreWithTypicalValues() {
        GithubRepository repo = new GithubRepository(
                "repo1",
                "user/repo1",
                "A test repo",
                25000, // stars
                5000,  // forks

                Instant.now().minus(Duration.ofDays(1)), // updated 1 day ago
                "Java",
                0.0
        );

        GithubRepository scored = strategy.calculateScore(repo);

        assertTrue(scored.popularityScore() >= 0 && scored.popularityScore() <= 100);
        assertTrue(scored.popularityScore() > 0);
    }

    @Test
    void testCalculateScoreWithOldRepositoryLowRecencyScore() {
        GithubRepository repo = new GithubRepository(
                "repo3",
                "user/repo3",
                "Old repo",
                1000,
                500,
                Instant.now().minus(Duration.ofDays(90)),
                "C++",
                0.0
        );

        GithubRepository scored = strategy.calculateScore(repo);

        // Should be small because recency is very low
        assertTrue(scored.popularityScore() > 0);
        assertTrue(scored.popularityScore() < 30);
    }

    @Test
    void testCalculateScoreRecentUpdateHigherRecencyScore() {
        GithubRepository recent = new GithubRepository(
                "repo4",
                "user/repo4",
                "Recently updated repo",
                5000,
                500,
                Instant.now().minus(Duration.ofDays(1)),
                "Go",
                0.0
        );

        GithubRepository old = new GithubRepository(
                "repo5",
                "user/repo5",
                "Old repo",
                5000,
                500,
                Instant.now().minus(Duration.ofDays(30)),
                "Go",
                0.0
        );

        double recentScore = strategy.calculateScore(recent).popularityScore();
        double oldScore = strategy.calculateScore(old).popularityScore();

        assertTrue(recentScore > oldScore);
    }

    @Test
    void testCalculateScoreWithZeroStarsAndForksReturnsLowScore() {
        GithubRepository repo = new GithubRepository(
                "repo6",
                "user/repo6",
                "New repo",
                0,
                0,
                Instant.now().minus(Duration.ofDays(1)),
                "Rust",
                0.0
        );

        GithubRepository scored = strategy.calculateScore(repo);

        assertTrue(scored.popularityScore() > 0);
        assertTrue(scored.popularityScore() < 20);
    }

    @Test
    void testCalculateScoreExactlyAtMaxThresholds() {
        GithubRepository repo = new GithubRepository(
                "maxRepo", "user/maxRepo", "desc",
                50000, 10000, Instant.now(),
                "Python", 0.0
        );

        GithubRepository scored = strategy.calculateScore(repo);
        assertEquals(100.0, Math.round(scored.popularityScore()));
    }
}
