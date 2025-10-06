package com.githubpopularity.client;

import com.githubpopularity.dto.GithubRepositoryItem;

import java.util.List;

/**
 * Interface for interacting with the GitHub API.
 * <p>
 * Provides methods to fetch repositories using a search query with paging support.
 */
public interface GithubApiClient {

    /**
     * Fetches a list of GitHub repositories based on the given search query and paging parameters.
     *
     * @param query   search query string (e.g., "language:Java created:>2023-01-01")
     * @param perPage number of repositories to fetch per page
     * @param page    page number to fetch
     * @return list of {@link GithubRepositoryItem}; returns an empty list if no repositories match the query
     */
    List<GithubRepositoryItem> fetchRepositories(String query, int perPage, int page);
}