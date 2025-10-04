# github-repo-popularity
Spring Boot backend that scores GitHub repositories by stars, forks, and recency, exposed via a REST API.

## Setup

1. **Clone the repository**
2. **Configure application properties**
   - Edit `src/main/resources/application.properties` to set the GitHub API base URL and default page size if needed.
3. **Build and run**
   - Using Maven Wrapper:
     ```
     ./mvnw spring-boot:run
     ```
   - Or build a jar:
     ```
     ./mvnw clean package
     java -jar target/github-popularity-0.0.1-SNAPSHOT.jar
     ```

## Configuration

In `src/main/resources/application.properties`:
```
github.api.base-url=https://api.github.com
github.api.default-per-page=10
```

## API Usage

### Get Popular Repositories

```
GET /api/repositories/popularity?language={language}&createdAfter={YYYY-MM-DD}&page={page}&perPage={perPage}
```

- `language` (required): Programming language (e.g., Java)
- `createdAfter` (required): Earliest creation date (format: YYYY-MM-DD)
- `page` (optional, default: 1): Page number
- `perPage` (optional, default: 10): Results per page

**Example:**
```
curl "http://localhost:8080/api/repositories/popularity?language=Java&createdAfter=2023-01-01&page=1&perPage=5"
```

### Response
A JSON array of repositories, each with a calculated popularity score.

## Error Handling
- Returns `400 Bad Request` for invalid parameters (e.g., missing or malformed `language` or `createdAfter`).
- Returns an empty list if the GitHub API is unavailable or rate-limited (errors are logged).

## Testing

To run all tests:
```
./mvnw test
```

---

For questions or contributions, please open an issue or pull request.
