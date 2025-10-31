
# prompt.md

**Project Title:** Our Voice, Our Rights — Minimal Render Backend

---

## Goal

Create a minimal **Spring Boot 3.3.x (Java 21)** backend with **one GET endpoint**:

```
GET /resource/ee03643a-ee4c-48c2-ac30-9f2ff26ab722
```

This endpoint acts as a **proxy** or **mock handler** for MGNREGA data from Data.gov.in.
It accepts query parameters like `api-key`, `format`, `offset`, `limit`, and `filters[...]`, validates them, and returns either a real API call result or dummy response data.

The backend must be **ready for deployment on Render** with a **Dockerfile** and **Render deploy instructions**.

---

## Features

1. **Single endpoint:** `/resource/ee03643a-ee4c-48c2-ac30-9f2ff26ab722`
2. Accepts query parameters:

   | Parameter               | Type                           | Description                                         |
   | ----------------------- | ------------------------------ | --------------------------------------------------- |
   | `api-key`             | `string` *(required)*      | API key for Data.gov.in (sample key provided below) |
   | `format`              | `string` *(default: json)* | Output format (`json`, `xml`, or `csv`)       |
   | `offset`              | `integer` *(optional)*     | Records to skip for pagination                      |
   | `limit`               | `integer` *(optional)*     | Number of records to return                         |
   | `filters[state_name]` | `string` *(optional)*      | Filter by state name                                |
   | `filters[fin_year]`   | `string` *(optional)*      | Filter by financial year                            |

   **Sample API key:**


   ```
   579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b
   ```
3. Returns static mock JSON (for now), shaped like a DataGov response.
4. Validates `api-key` and `format` parameters.
5. Uses **Springdoc OpenAPI** for Swagger UI at `/swagger-ui.html`.
6. No caching, no database required (can connect to MySQL later if desired).
7. Dockerized for easy Render deployment.

---

## Example API

### Request

```
GET /resource/ee03643a-ee4c-48c2-ac30-9f2ff26ab722?api-key=579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b&format=json&limit=10
```

### Response

```json
{
  "resourceId": "ee03643a-ee4c-48c2-ac30-9f2ff26ab722",
  "status": "ok",
  "query": {
    "api-key": "579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b",
    "format": "json",
    "limit": 10
  },
  "records": [
    {
      "state_name": "KARNATAKA",
      "district_name": "BENGALURU RURAL",
      "fin_year": "2023-2024",
      "month": "APRIL",
      "total_persondays": 12345,
      "total_households": 567,
      "expenditure": 98000
    }
  ]
}
```

### Error Responses

| Code    | Description                         |
| ------- | ----------------------------------- |
| `400` | Missing or invalid parameters       |
| `403` | Invalid or unauthorized `api-key` |

---

## Project Structure

```
backend/
 ├── src/main/java/com/example/backend/controller/ResourceController.java
 ├── src/main/resources/application.yml
 ├── pom.xml
 ├── Dockerfile
 ├── render.yaml
 ├── docker-compose.yml (optional for local run)
 └── README.md
```

---

## OpenAPI (Swagger)

* Automatically generated via **Springdoc**.
* Accessible at:

  ```
  http://localhost:8080/swagger-ui.html
  ```

  or in Render:

  ```
  https://<your-app-name>.onrender.com/swagger-ui.html
  ```

---

## Environment Variables

| Variable             | Description                                            |
| -------------------- | ------------------------------------------------------ |
| `PORT`             | Port Render binds to (Spring Boot reads automatically) |
| `DATA_GOV_API_KEY` | API key for DataGov (optional; default sample key)     |

---

## Dockerfile

Simple container for Render deployment:

```dockerfile
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests
EXPOSE 8080
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]
```

---

## render.yaml (Render Deployment Config)

```yaml
services:
  - type: web
    name: our-voice-backend
    env: docker
    plan: free
    autoDeploy: true
    envVars:
      - key: DATA_GOV_API_KEY
        value: 579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b
```

---

## Deploying on Render

1. Push your project to GitHub.
2. Go to [https://render.com](https://render.com) → **New → Web Service**
3. Connect your GitHub repo.
4. Choose **Docker** as deployment method.
5. Set environment variable:

   ```
   DATA_GOV_API_KEY=579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b
   ```
6. Deploy 🎉
   Render will automatically expose your API at:

   ```
   https://<your-app-name>.onrender.com/resource/ee03643a-ee4c-48c2-ac30-9f2ff26ab722
   ```

---

## Local Run (Optional)

```bash
./mvnw spring-boot:run
```

or via Docker:

```bash
docker build -t our-voice-backend .
docker run -p 8080:8080 our-voice-backend
```

---

## Acceptance Criteria

✅ Endpoint `/resource/ee03643a-ee4c-48c2-ac30-9f2ff26ab722` works
✅ Query parameters are validated and echoed
✅ Swagger UI renders successfully
✅ Ready for Render deployment via Docker

---

Would you like me to generate the **actual Spring Boot code files** (`ResourceController.java`, `pom.xml`, `application.yml`, and Dockerfile`) for this Render-ready version next?
