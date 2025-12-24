# Development Guide (Docker + Profiles)

This guide explains how to run the project locally and with Docker Compose, and how `application.yaml` and `application-dev.yaml` work together.

## Profiles and Config Files

- `src/main/resources/application.yaml` holds default, environment-agnostic settings.
- `src/main/resources/application-dev.yaml` holds **dev-only** settings (e.g., Swagger, datasource defaults).
- `spring.profiles.active` is **not** hard-coded in YAML. You must set the profile via environment variable.
- `application-dev.yaml` imports an optional `.env` file from the project root for local secrets.

Example:
```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

## Local Run (no Docker)

1) Ensure PostgreSQL is running locally.
2) Use the dev profile:
```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```
or
```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw -DskipTests spring-boot:run
```

### Default dev datasource values
`application-dev.yaml` uses env vars with fallbacks:
- `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://localhost:5432/thatmoment_db`)
- `SPRING_DATASOURCE_USERNAME` (default: `postgres`)
- `SPRING_DATASOURCE_PASSWORD` (default: `postgres`)

Override them as needed:
```bash
SPRING_PROFILES_ACTIVE=dev \
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/thatmoment_db \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
./mvnw spring-boot:run
```

### Mail config (env only)
`application.yaml` defines `spring.mail` (SMTP) and `app.mail` (from address/name) with environment placeholders.
Put real values in a local `.env` file or your shell environment.

### Redis defaults (dev)
`application-dev.yaml` uses env vars with fallbacks:
- `SPRING_REDIS_HOST` (default: `localhost`)
- `SPRING_REDIS_PORT` (default: `6379`)

## Docker Compose Run

`docker-compose.yml` defines:
- `db` (PostgreSQL 16) with a **named volume** for persistence
- `app` (Maven + Java 21) running `./mvnw spring-boot:run`

Start everything:
```bash
docker compose up
```

Start only DB:
```bash
docker compose up db
```

Stop services (data stays):
```bash
docker compose down
```

Remove data (only if you really want to reset DB):
```bash
docker compose down -v
```

### Notes about persistence
The DB data is stored in the named volume `thatmoment_pgdata`. That volume survives `docker compose down` and prevents data loss between runs.

## Swagger / OpenAPI (dev only)

Swagger UI and OpenAPI docs are enabled **only** in the `dev` profile.

- Swagger UI:
  - `http://localhost:8080/swagger-ui.html`
  - `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON:
  - `http://localhost:8080/v3/api-docs`

## Common Errors

### "Failed to configure a DataSource"
This happens when no profile is active or datasource properties are missing.
Fix by running with `SPRING_PROFILES_ACTIVE=dev`, or define the datasource environment variables.


## Redis
• Redis dependency’sini ve Docker Compose servislerini ekledim.

Notlar:

- docker compose up ile Redis de otomatik ayağa kalkar.
- App icin SPRING_REDIS_HOST=redis ve SPRING_REDIS_PORT=6379 docker icinde setli.

Istersen application-dev.yaml icine Redis default’larini da ekleyebilirim (lokalde localhost icin).
