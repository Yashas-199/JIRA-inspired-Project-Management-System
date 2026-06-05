Integration guide — ProjectManagementSystem

This file explains how to bring up the full stack locally on Windows (PowerShell) using Docker (for MySQL), the Maven wrapper for the backend, and Vite for the frontend.

Prerequisites
- Docker Desktop installed and running (or Docker Engine)
- Java 21 (for running the backend locally with the wrapper)
- Node.js (for frontend dev server)
- Git (repo already cloned)

Steps

1) Start the database (Docker)

Open PowerShell in the repository root and run:

```powershell
# Start MySQL container in detached mode
docker compose up -d
```

Notes:
- The file `.env` contains the DB credentials used by the Spring Boot `application.properties`:
  - DB: project_management
  - USER: devuser
  - PASSWORD: Yash@2005
- The `schema.sql` is mounted into `/docker-entrypoint-initdb.d/` so on first startup MySQL will run the script and create the schema.

2) Verify DB is running and initialized

```powershell
# Check container status
docker ps --filter "name=pms-mysql"
# Tail logs (see initialization output)
docker logs -f pms-mysql
```

3) Start backend (Spring Boot)

From repo root you can run the wrapper directly (PowerShell):

```powershell
# Run without changing directory
.\backend\pms\mvnw.cmd spring-boot:run

# Or change directory then run
Set-Location .\backend\pms
.\mvnw.cmd spring-boot:run
```

If you prefer system Maven (and have it on PATH):

```powershell
# from backend\pms
mvn spring-boot:run
```

Expected logs:
- HikariPool or datasource connection information
- Hibernate/DML statements (because `spring.jpa.show-sql=true`)

4) Start frontend (Vite)

```powershell
Set-Location .\frontend
npm install
npm run dev
```

The dev server will run on http://localhost:5173 and Vite is configured to proxy `/api` to `http://localhost:8080`.

5) Verify end-to-end

- Open http://localhost:5173 in your browser.
- Use DevTools Network tab to confirm API requests go to `/api/...` and are proxied to the backend.
- Or test a backend endpoint directly:

```powershell
# Example: list projects endpoint (adjust path if needed)
Invoke-RestMethod -Uri http://localhost:8080/api/projects -Method Get
```

Stopping everything

```powershell
# Stop frontend (Ctrl+C in its terminal)
# Stop backend (Ctrl+C in its terminal)
# Stop and remove DB container
docker compose down
```

Troubleshooting

- If the backend fails to connect to MySQL, confirm the container is running and the credentials in `backend/pms/src/main/resources/application.properties` match `.env`.
- If the schema was not applied, the MySQL init scripts run only on first container start. If you need to re-run, remove the volume (`docker compose down -v`) then `docker compose up -d` again.
- "mvnw.cmd not recognized": make sure you include the path and `.\ackend\pms\mvnw.cmd` or change to that dir before running.
- If ports 3306 or 8080 are in use, stop the conflicting services or change ports in `application.properties` and `vite.config.js`.

Next steps I can take for you
- Run `docker compose up -d` here and monitor startup logs.
- Help containerize the backend with a Dockerfile and add it to `docker-compose.yml` so the whole stack can be started with one command.
- Remove `DROP DATABASE` lines from `schema.sql` if you prefer not to allow the script to reset the DB on import.

If you want me to run the Docker compose now, say "please run the compose" and I will start it and report results.