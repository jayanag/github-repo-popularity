package com.githubpopularity.service;

import com.githubpopularity.model.GithubRepository;

import java.util.List;

/**
 * Service interface for fetching repositories from GitHub.
 */
public interface GithubRepositoryService {

    /**
     * Fetches repositories from GitHub using language and creation date filters, with pagination.
     *
     * @param language programming language to filter by
     * @param createdAfter earliest creation date (ISO format, e.g., "2023-01-01")
     * @param page page number (1-based)
     * @param perPage number of results per page
     * @return list of repositories
     */
    List<GithubRepository> fetchRepositories(String language, String createdAfter, int perPage, int page);
}
