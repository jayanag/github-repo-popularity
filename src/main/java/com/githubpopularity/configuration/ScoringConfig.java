package com.githubpopularity.configuration;

import com.githubpopularity.service.PopularityScoringStrategy;
import com.githubpopularity.service.StaticThresholdStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScoringConfig {
    @Bean
    public PopularityScoringStrategy popularityScoringStrategy() {
        return new StaticThresholdStrategy();
    }
}
