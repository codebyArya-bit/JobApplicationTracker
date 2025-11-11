# Job Application Tracker

A production‑ready Java 21 Spring Boot web app with a clean HTML UI (Thymeleaf) and JSON API. Stores data in memory (resets on restart) and supports CSV export/import. Ready to run locally or via Docker to Render/Railway/Fly.io/Cloud Run/Heroku.

## Features
- Add / edit / delete job applications
- Filter & search (status, company, text search)
- Auto Follow‑Up Due flag (7 days after last update)
- CSV export & CSV import
- JSON API for programmatic use
- Health endpoint (`/actuator/health`)

## Tech
- Spring Boot 3.3.x
- Java 21
- Thymeleaf for server‑rendered UI
- In‑memory store (ConcurrentHashMap)

## Run Locally
Requires Java and Maven installed.

```bash
# From job-tracker directory
mvn spring-boot:run
# App runs on http://localhost:8082/
```

Or Docker:

```bash
# Build image
docker build -t job-tracker:latest .
# Run container
docker run --rm -p 8082:8082 job-tracker:latest
```

## Project Structure
```
job-tracker/
├─ pom.xml
├─ Dockerfile
├─ Procfile                # (Heroku optional)
├─ README.md
└─ src/
   ├─ main/java/com/example/jt/
   │  ├─ Application.java
   │  ├─ model/ApplicationRecord.java
   │  ├─ model/Status.java
   │  ├─ service/AppService.java
   │  ├─ web/AppController.java        # UI (Thymeleaf)
   │  └─ web/ApiController.java        # JSON API
   └─ main/resources/
      ├─ application.properties
      ├─ templates/
      │  ├─ layout.html
      │  ├─ index.html
      └─ static/
         └─ app.css
```

## Configuration
- Port: `8082` (set in `src/main/resources/application.properties`)
- Actuator: `health` and `info` exposed

## JSON API
Base: `http://localhost:8082/api/apps`

- List: `GET /api/apps?q=&status=`
- Get: `GET /api/apps/{id}`
- Create: `POST /api/apps`
  - Body:
  ```json
  {
    "company": "Acme",
    "role": "Senior Engineer",
    "status": "APPLIED",
    "appliedOn": "2025-01-10",
    "notes": "referral"
  }
  ```
- Update: `PUT /api/apps/{id}` (same fields plus optional `lastUpdate`)
- Delete: `DELETE /api/apps/{id}`
- Export CSV: `GET /api/apps/export`
- Import CSV: `POST /api/apps/import` (multipart field `file`)

## UI
- Home page: filter/search, add new application, table with follow‑up badge, delete action
- Export CSV link on header; import CSV via form

## CSV Format
Header: `id,company,role,status,appliedOn,notes,lastUpdate`
- Fields with commas/newlines/quotes are quoted and escaped
- `lastUpdate` optional; if blank, defaults to `appliedOn`

## Heroku
- Uses `Procfile`: `web: java -Dserver.port=$PORT -jar target/job-tracker-0.0.1.jar`
- Add Java buildpack; deploy then run `mvn -DskipTests package` during build

## Health
- `GET /actuator/health`

## Notes
- Data resets on restart.
- Validation via Jakarta annotations on `ApplicationRecord` and request DTOs.