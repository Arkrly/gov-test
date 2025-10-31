# Our Voice, Our Rights — Minimal Render Backend

A minimal Spring Boot 3.3 backend that proxies/mock handles the `ee03643a-ee4c-48c2-ac30-9f2ff26ab722` resource from Data.gov.in. The service validates incoming query parameters, returns a static payload shaped like the upstream API, and is prepped for deployment on [Render](https://render.com) via Docker.

## Features
- Single GET endpoint at `/resource/ee03643a-ee4c-48c2-ac30-9f2ff26ab722`
- Required `api-key` validation against `DATA_GOV_API_KEY`
- Optional query parameters for `format`, `offset`, `limit`, and filters (`filters[state_name]`, `filters[fin_year]`)
- Springdoc OpenAPI UI at `/swagger-ui.html`
- Dockerfile and `render.yaml` for Render deployment

## Getting Started

### Prerequisites
- Java 21+
- Docker (optional for containerized run)

### Local Run
```powershell
./mvnw spring-boot:run
```
Then visit `http://localhost:8080/resource/ee03643a-ee4c-48c2-ac30-9f2ff26ab722?api-key=...` with a valid key.

Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

### Tests
```powershell
./mvnw test
```

## Docker
Build and run locally:
```powershell
docker build -t our-voice-backend .
docker run -p 8080:8080 --env DATA_GOV_API_KEY=579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b our-voice-backend
```

## Render Deployment
1. Push this folder to GitHub.
2. Create a new Web Service on Render using "Deploy an existing Dockerfile".
3. Point Render at the repository root/folder containing this backend.
4. Render reads `render.yaml` and the Dockerfile automatically.
5. Set `DATA_GOV_API_KEY` in the dashboard if you need a value different from the sample.

Once deployed, your API is reachable at:
```
https://<your-app-name>.onrender.com/resource/ee03643a-ee4c-48c2-ac30-9f2ff26ab722
```

## Environment
- `PORT`: provided by Render, automatically wired via `application.yml`
- `DATA_GOV_API_KEY`: defaults to the sample key but should be overridden in production
