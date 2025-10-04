package com.githubpopularity.service;

import com.githubpopularity.model.GithubRepository;
import java.util.List;

public interface PopularityScoringStrategy {
    GithubRepository calculateScore(GithubRepository repository);

    default List<GithubRepository> calculateScores(List<GithubRepository> repos) {
        return repos.stream()
                .map(this::calculateScore)
                .toList();
    }
}