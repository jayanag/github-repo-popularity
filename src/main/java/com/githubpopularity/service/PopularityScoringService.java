package com.githubpopularity.service;

import com.githubpopularity.model.GithubRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PopularityScoringService {

    private final PopularityScoringStrategy scoringStrategy;

    public PopularityScoringService(PopularityScoringStrategy scoringStrategy) {
        this.scoringStrategy = scoringStrategy;
    }

    public List<GithubRepository> scoreRepositories(List<GithubRepository> repos) {
        return scoringStrategy.calculateScores(repos);
    }
}
