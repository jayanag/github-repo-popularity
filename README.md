# GitHub Repository Popularity Backend

A Spring Boot backend service that fetches GitHub repositories, calculates their popularity based on stars, forks, and recency, and exposes this information via a REST API. Designed for clarity, scalability, and ease of extension.

## Features
- Fetches repositories from the GitHub public API and calculates a popularity score based on:
  - Stars (weight: 60%)
  - Forks (weight: 30%)
  - Recency of last update (weight: 10%)
- Supports filtering by programming language and creation date.
- Supports pagination via `perPage` and `page` query parameters.
- Comprehensive error handling and validation.
- Fully extensible for caching, custom scoring strategies, and parallel API calls.
- Integrated logging for monitoring and debugging.

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Internet access for GitHub API

### Setup

1. **Clone the repository**
   ```sh
   git clone https://github.com/jayanag/github-repo-popularity.git
   cd github-popularity
   ```

2. **Configure application properties**
   Edit `src/main/resources/application.properties`:
   ```properties
   # GitHub API
   github.api.base-url=https://api.github.com

   # Threshold values for popularity scoring
   popularity.static.max-stars=50000
   popularity.static.max-forks=10000
   popularity.static.recency-decay-days=3
   ```

3. **Build and run**
   - Using Maven Wrapper:
     ```sh
     ./mvnw spring-boot:run
     ```
   - Or build a JAR:
     ```sh
     ./mvnw clean package
     java -jar target/github-popularity-0.0.1-SNAPSHOT.jar
     ```

## API Usage

### Get Popularity scores
```
GET /api/repositories/popularity
```

#### Query Parameters
| Parameter    | Required | Description                                 |
|--------------|----------|---------------------------------------------|
| language     | Yes      | Programming language (e.g., Java)           |
| createdAfter | Yes      | Earliest creation date (YYYY-MM-DD)         |
| perPage      | No       | Number of results per page (default: 10)    |
| page         | No       | Page number (default: 1)                    |

#### Example
```sh
curl "http://localhost:8080/api/repositories/popularity?language=Java&createdAfter=2023-01-01&perPage=5&page=1"
```

#### Response
- **HTTP 200 OK**: JSON array of repositories and their popularity scores.
- **HTTP 204 No Content**: No repositories found.
- Errors are returned in JSON with proper status codes and messages.

## Error Handling
| Status Code | Description                                      |
|-------------|--------------------------------------------------|
| 400         | Invalid request parameters (e.g., blank language or wrong date format) |
| 403         | GitHub API rate limit exceeded                   |
| 422         | GitHub API validation error                      |
| 503         | GitHub API unavailable                           |
| 500         | Internal server error                            |

All errors return a structured JSON object with:
- `timestamp`
- `status`
- `error`
- `path`
- `errors` (details)

## Extensibility
- **Popularity scoring strategy**: Can be replaced or extended. 
- **Caching**: Can be added (e.g., Redis, Caffeine) to reduce API calls for repeated queries.
- **Pagination**: Supports `perPage` and `page` parameters.
- **Logging**: Logs repository fetches, mappings, and scoring details.

## Trade-offs
- Uses blocking WebClient calls for simplicity; can be made fully reactive for high concurrency.
- No authentication by default — limited by GitHub public API rate limits. Tokens can be added for higher limits.

## Testing
- Unit and integration tests included.
- Run all tests with:
  ```sh
  ./mvnw test
  ```

## Future Enhancements
- Add caching for frequent queries.
- Parallelize/batch GitHub API requests for faster responses.
- Support additional repository filters (e.g., stars, forks, topics).
- Implement rate limiting to handle GitHub API restrictions gracefully.
- Add retry logic for transient failures to improve reliability.
- Expose OpenAPI/Swagger documentation.
- Add metrics and monitoring endpoints.
- Containerize with Docker/Kubernetes for cloud deployment.
