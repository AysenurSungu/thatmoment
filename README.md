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

Docs
- DEVELOPMENT.md (local + docker usage)
- STANDARDS.md (project rules)
