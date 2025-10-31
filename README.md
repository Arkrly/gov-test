# Our Voice, Our Rights – Backend

Production-ready Spring Boot backend (Java 21) that ingests MGNREGA metrics, persists them in MySQL, caches hot endpoints, and exposes geo-enabled APIs.

## Features
- Scheduled ingestion pipeline with Resilience4j retry & circuit breaker guarding the DataGov API.
- Domain services for states, districts, performance, comparison, and geo-detection.
- Cache abstraction supporting Redis or Caffeine via `CACHE_PROVIDER`.
- Standardized error payloads, validation, and rate limiting filter.
- OpenAPI 3.0 documentation, Actuator endpoints, and Prometheus metrics.
- Docker/Docker Compose setup for local development (backend, MySQL, optional Redis).

## Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- Docker & Docker Compose (optional but recommended)
- MySQL 8.x (if not using Docker Compose)

### Configuration

This is a Spring Boot (Maven) application. Configuration values live in `src/main/resources/application.properties` (converted from the original YAML). You can override any property with environment variables — the default values used by the project are shown below.

Key environment variables / properties (defaults):

| Variable / Property | Description | Default |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL for MySQL | `jdbc:mysql://localhost:3306/ourvoiceourrights?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` | MySQL user | `root` |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password | `password` |
| `DATA_GOV_API_KEY` | API key for data.gov.in (set as env var) | _required_ |
| `INGEST_LIMIT` / `app.ingest-limit` | Max records per fetch page | `100` |
| `INGEST_SCHEDULE_CRON` / `app.ingestion-schedule-cron` | Cron for ingestion | `0 0 */6 * * *` |
| `CACHE_PROVIDER` / `spring.cache.type` | `caffeine` or `redis` | `caffeine` |
| `GEOIP2_DB_PATH` / `app.geo.geo-ip-database-path` | Path to GeoLite2 City DB | _(blank)_ |
| `REVERSE_GEOCODE_BASE` / `app.geo.reverse-geocode-base-url` | Reverse geocode endpoint | _(blank)_ |
| `RATE_LIMIT_RPM` / `app.rate-limit.requests-per-minute` | Requests per minute cap | `120` |

You can set these in a `.env` file, export them in your shell, or provide them using your platform's secret/config system.

#### .env example

A ready-to-use example file has been added at the project root: `.env.example`. Copy it to `.env` and edit the values before running locally:

PowerShell (Windows):
```powershell
# Load environment variables from .env into current PowerShell session (requires PowerShell 7+)
Get-Content .env | ForEach-Object {
	if ($_ -and -not $_.StartsWith('#')) {
		$pair = $_ -split '=', 2
		if ($pair.Length -eq 2) { Set-Item -Path Env:$($pair[0]) -Value $pair[1].Trim('"') }
	}
}
```

Linux/macOS (bash):
```bash
export $(grep -v '^#' .env | xargs)
```

If you're using Docker Compose the `.env` file will be automatically picked up by Compose.

### Build & Run
```pwsh
# run tests
mvn clean verify

# run locally
mvn spring-boot:run

# docker compose (backend + mysql + redis)
docker compose up --build
```

### Deploying to Render

We ship a [`render.yaml`](render.yaml) blueprint and a Docker-based image that are ready for Render's deployment platform:

1. Push the repository to GitHub/GitLab and connect it to Render.
2. Choose **Blueprint** deployment and point Render at `render.yaml`.
3. Render will provision (a) a managed MySQL database and (b) a Docker web service built from the root `Dockerfile` using the `render/entrypoint.sh` script to normalise the database environment variables.
4. After the initial deploy finishes:
	 - Set `DATA_GOV_API_KEY` and any other secrets in the Render dashboard (the blueprint marks it as a shared secret/unsynced var).
	 - Upload `GeoLite2-City.mmdb` to a persistent disk or object storage and adjust `GEOIP2_DB_PATH` if needed.
5. The service exposes `/actuator/health` for Render's health checks and listens on port 8080 inside the container.

For manual deploys you can also run the image locally:

```bash
docker build -t ourvoice-backend .
docker run --rm -p 8080:8080 \
	-e SPRING_DATASOURCE_URL='jdbc:mysql://host/db' \
	-e SPRING_DATASOURCE_USERNAME='user' \
	-e SPRING_DATASOURCE_PASSWORD='pass' \
	ourvoice-backend
```

### Database Seed
A sample seed script lives in `scripts/seed.sql`. Run it after MySQL starts:
```pwsh
mysql -h 127.0.0.1 -P 3306 -u root -p ourvoiceourrights < scripts/seed.sql
```

### OpenAPI & Postman
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI spec: `openapi/openapi.yaml`
- Postman collection: `postman/OurVoiceOurRights.postman_collection.json`

## Testing
- Unit tests cover ingestion hashing and controller validation.
- Integration tests use Testcontainers for MySQL.
- Execute `mvn verify` to run the full suite.

## Deployment Notes
- Configure MySQL credentials & Redis connection in environment.
- Use Prometheus scrape endpoint at `/actuator/prometheus`.
- Ensure GeoLite2 DB is mounted to `/app/geoip` when running in Docker.

## Frontend Integration Notes
- Use `/api/states` & `/api/districts` to populate dropdowns.
- `/api/performance/{district}` returns latest metrics (optionally by fin year).
- `/api/performance/{district}/history` provides timeline (filterable).
- `/api/compare` yields delta metrics for two districts.
- `/api/geo/detect` helps implement “Detect my district” with optional coordinates.
