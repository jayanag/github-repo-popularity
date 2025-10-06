package com.githubpopularity.service;

import com.githubpopularity.model.GithubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticThresholdStrategyTest {

    private StaticThresholdStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new StaticThresholdStrategy(50000, 10000, 3);
    }

    @Test
    void testCalculateScoreHighStarsAndForksRecentRepo() {
        GithubRepository repo = new GithubRepository(
                "repo1", "owner/repo1", "test repo",
                50000,
                10000,
                Instant.now().minusSeconds(3600),
                "Java", 0.0
        );

        GithubRepository scored = strategy.calculateScore(repo);

        assertTrue(scored.popularityScore() > 99.0 && scored.popularityScore() <= 100.0,
                "Score should be near 100 for max stars/forks and recent update");
    }

    @Test
    void testCalculateScore_NoStarsOrForks() {
        GithubRepository repo = new GithubRepository(
                "repo2", "owner/repo2", "test repo",
                0, 0,
                Instant.now().minusSeconds(86400 * 10), // 10 days old
                "Python", 0.0
        );

        GithubRepository scored = strategy.calculateScore(repo);

        assertTrue(scored.popularityScore() >= 0 && scored.popularityScore() < 5,
                "Low score expected for no stars/forks and old repo");
    }

    @Test
    void testCalculateScore_PartialStarsAndForks() {
        GithubRepository repo = new GithubRepository(
                "repo3", "owner/repo3", "test repo",
                25000, 5000, // half of max
                Instant.now().minusSeconds(86400), // 1 day ago
                "Go", 0.0
        );

        GithubRepository scored = strategy.calculateScore(repo);

        assertTrue(scored.popularityScore() >= 50 && scored.popularityScore() <= 80,
                "Moderate score expected for half stars/forks and recent repo");
    }

    @Test
    void testCalculateScore_OldRepo_LowRecencyScore() {
        GithubRepository repo = new GithubRepository(
                "repo4", "owner/repo4", "test repo",
                50000, 10000,
                Instant.now().minusSeconds(86400 * 60), // 60 days old
                "Rust", 0.0
        );

        GithubRepository scored = strategy.calculateScore(repo);

        assertTrue(scored.popularityScore() > 85 && scored.popularityScore() < 100,
                "Score should remain high but slightly reduced due to age");
    }

    @Test
    void testRecencyEffectRecentVsOld() {
        GithubRepository recent = new GithubRepository(
                "repo5", "owner/repo5", "recent",
                1000, 500,
                Instant.now().minusSeconds(3600), // 1 hour ago
                "C#", 0.0
        );

        GithubRepository old = new GithubRepository(
                "repo6", "owner/repo6", "old",
                1000, 500,
                Instant.now().minusSeconds(86400 * 30), // 30 days old
                "C#", 0.0
        );

        double recentScore = strategy.calculateScore(recent).popularityScore();
        double oldScore = strategy.calculateScore(old).popularityScore();

        assertTrue(recentScore > oldScore,
                "Recent repository should have higher score than old one");
    }

    @Test
    void testScoreNeverExceeds100() {
        GithubRepository repo = new GithubRepository(
                "repo7", "owner/repo7", "overflow test",
                999999, 999999,
                Instant.now(),
                "Java", 0.0
        );

        GithubRepository scored = strategy.calculateScore(repo);

        assertTrue(scored.popularityScore() <= 100,
                "Score must never exceed 100 even for large star/fork values");
    }

    @Test
    void testScoreStableForEqualInputs() {
        GithubRepository repo1 = new GithubRepository(
                "repo8", "owner/repo8", "consistency test",
                1000, 100,
                Instant.now().minusSeconds(86400 * 2),
                "Java", 0.0
        );

        GithubRepository repo2 = new GithubRepository(
                "repo9", "owner/repo9", "consistency test",
                1000, 100,
                repo1.lastUpdated(),
                "Java", 0.0
        );

        double score1 = strategy.calculateScore(repo1).popularityScore();
        double score2 = strategy.calculateScore(repo2).popularityScore();

        assertEquals(score1, score2, 0.0001,
                "Two repos with identical inputs must produce the same score");
    }
}
