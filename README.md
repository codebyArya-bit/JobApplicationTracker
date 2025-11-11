# Job Application Tracker

## Live Deployment
- Public URL: https://jobapplicationtracker-fvh8.onrender.com
- UI: https://jobapplicationtracker-fvh8.onrender.com/ui/index.html
- Health: https://jobapplicationtracker-fvh8.onrender.com/actuator/health

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

UI (SPA):

- Open `http://localhost:8082/ui/index.html` for the single-page UI.
- JSON API base: `http://localhost:8082/api/apps`.

Alternatively, build the JAR and run it:

```bash
mvn -DskipTests package
java -jar target/job-tracker-0.0.1.jar
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
      │  ├─ home.html
      │  ├─ test.html
      │  └─ home_probe.html
      └─ static/
         ├─ app.css
         └─ ui/
            ├─ index.html
            └─ app.js
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
- SPA UI: `http://localhost:8082/ui/index.html` (filter/search, add, delete)
- Export CSV link: `http://localhost:8082/api/apps/export`
- Import CSV: POST `http://localhost:8082/api/apps/import` (multipart field `file`)

## CSV Format
Header: `id,company,role,status,appliedOn,notes,lastUpdate`
- Fields with commas/newlines/quotes are quoted and escaped
- `lastUpdate` optional; if blank, defaults to `appliedOn`

## Heroku
- Uses `Procfile`: `web: java -Dserver.port=$PORT -jar target/job-tracker-0.0.1.jar`
- Add Java buildpack; deploy then run `mvn -DskipTests package` during build

Steps:

1) Create a Heroku app and enable the Java buildpack.
2) Push the repo to Heroku or connect GitHub.
3) Set up a build step that runs `mvn -DskipTests package`.
4) Heroku will run the `web` process from `Procfile` binding to `$PORT`.
5) Visit the app, then open `/ui/index.html`.

Note: In Heroku, do not hardcode the port; rely on `$PORT` in `Procfile`.

## Deploy with Docker (Any cloud)

Deploy the Docker image to providers that support containers (Render, Railway, Fly.io, Cloud Run):

```bash
# Build and publish your image (example using Docker Hub)
docker build -t <your-dockerhub-username>/job-tracker:latest .
docker push <your-dockerhub-username>/job-tracker:latest
```

Then create a service on your provider using the image. Ensure the service exposes container port `8082` (the app binds to `8082` by default). After deployment, open `/ui/index.html`.

### Render (Docker)
- Create a new Web Service
- Select “Deploy an existing image” or connect repo and let Render build using `Dockerfile`
- Service port: 8082
- Health check path: `/actuator/health`

### Railway (Dockerfile)
- Create a new service from your GitHub repo
- Railway will detect the `Dockerfile` and build the image
- Set service port to `8082`

## Health
- `GET /actuator/health`

## Notes
- Data resets on restart.
- Validation via Jakarta annotations on `ApplicationRecord` and request DTOs.