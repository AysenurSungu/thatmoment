ThatMoment Backend

Java 21 + Spring Boot backend for ThatMoment.

Quick Start
1) Run with Docker (app + db + redis)
   docker compose up
2) Run locally (dev profile)
   SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

Swagger (dev only)
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/swagger-ui/index.html

OpenAPI JSON
- http://localhost:8080/v3/api-docs

Profiles
- spring.profiles.active is set via environment variable
  Example: SPRING_PROFILES_ACTIVE=dev

## Project Rules (Required)
Before making any change, read:
- **AGENTS.md** (how to work in this repo)
- **BE-STANDARDS.md** (backend coding + architecture standards)

PRs must follow these rules. If something conflicts, **AGENTS.md + BE-STANDARDS.md** are the source of truth.

