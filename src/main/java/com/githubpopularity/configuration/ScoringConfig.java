package com.githubpopularity.configuration;

import com.githubpopularity.service.PopularityScoringStrategy;
import com.githubpopularity.service.StaticThresholdStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScoringConfig {

    @Value("${popularity.static.max-stars:50000}")
    private int maxStars;

    @Value("${popularity.static.max-forks:10000}")
    private int maxForks;

    @Value("${popularity.static.recency-decay-days:3}")
    private int recencyDecayDays;

    @Bean
    public PopularityScoringStrategy popularityScoringStrategy() {
        return new StaticThresholdStrategy(maxStars, maxForks, recencyDecayDays);
    }
}
