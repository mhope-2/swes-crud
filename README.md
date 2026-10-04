# Spring Boot REST API — Learning Project

A simple CRUD REST API built with Spring Boot for personal learning. Manages a list of software engineers stored in PostgreSQL. Technologies are normalized into reusable rows linked to engineers through a join table.

## Tech Stack

- Java 21
- Spring Boot 3.5
- Spring Data JPA + Hibernate
- PostgreSQL
- Apache Kafka via Spring Kafka
- Bean Validation
- JUnit 5 + Mockito

## Project Structure

```
src/main/java/com/michaelhope/
├── controller/    REST endpoints
├── service/       Business logic
├── repository/    Database access (JPA)
├── model/         JPA entities, including Technology
├── dto/           Request / Response records
├── mapper/        Entity ↔ DTO conversion
├── event/         Versioned Kafka event contracts and publisher
├── consumer/      Kafka consumers
└── exception/     ResourceNotFoundException + GlobalExceptionHandler
```

## Running the App

**1. Start PostgreSQL and Kafka**

```bash
docker compose up -d db kafka
```

This starts the infrastructure dependencies only. It avoids starting the
containerized `app` service when running the application locally with Maven.

**2. Run the application**

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

### Run with virtual threads

The virtual-thread profile is opt-in, so the default platform-thread
configuration remains available for comparison:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=virtual
```

The profile enables Spring Boot's virtual-thread support through
`src/main/resources/application-virtual.properties`.

### Using Dory instead of Docker or OrbStack

Dory provides a Docker-compatible engine and Compose v2, so this project does
not require a different Compose file. Install and open Dory, wait until its
engine is ready, then select its Docker context:

```bash
docker context use dory
dory doctor --active
docker compose up -d db kafka
./mvnw spring-boot:run -Dspring-boot.run.profiles=virtual
```

Dory publishes the PostgreSQL and Kafka ports to localhost, matching the
connection settings used by the local Maven run. Switch back to another
runtime by selecting its Docker context, for example:

```bash
docker context use orbstack
```

See the [Dory documentation](https://github.com/Augani/dory) for installation,
context setup, and runtime diagnostics.

## API Endpoints

| Method | Path                              | Description          |
|--------|-----------------------------------|----------------------|
| GET    | `/api/v1/software-engineer`       | Get all engineers    |
| GET    | `/api/v1/software-engineer/{id}`  | Get engineer by ID   |
| POST   | `/api/v1/software-engineer`       | Add a new engineer   |
| PUT    | `/api/v1/software-engineer/{id}`  | Update an engineer   |
| DELETE | `/api/v1/software-engineer/{id}`  | Delete an engineer   |
| GET    | `/api/v1/software-engineer/{id}/history` | Get CRUD event history |

CRUD mutations publish versioned events to `software-engineer.events.v2`, keyed
by engineer ID. The audit consumer stores them in the `engineer_audit` table.
History is eventually consistent with the CRUD response because Kafka
processing happens asynchronously.

The database schema is managed by the Flyway migrations described below. The
normalized technology tables are the canonical representation.

**Request body (POST / PUT):**

```json
{
  "name": "Alice",
  "technologies": ["Java", "Spring", "PostgreSQL"]
}
```

Technology names are trimmed, normalized case-insensitively, deduplicated, and returned in alphabetical order. PostgreSQL stores reusable `technology` rows and a `software_engineer_technology` join table.

## Database migrations

Flyway owns database schema changes. Versioned SQL migrations live in
`src/main/resources/db/migration` and are applied automatically when the
application starts. Hibernate is configured with `ddl-auto=validate`, so it
checks the Flyway-managed schema without changing it.

The current migration history is:

- `V1__create_base_schema.sql` creates or repairs the pre-normalization
  engineer and audit schema.
- `V2__normalize_technologies.sql` creates normalized technology tables and
  migrates legacy comma-separated `tech_stack` values.
- `V3__remove_legacy_tech_stack.sql` removes the legacy column after the
  normalized data has been written.

Existing non-empty development databases can be baselined at Flyway version `0`
and then run through the idempotent migration chain, but this is opt-in. The
Compose app service enables it explicitly with
`SPRING_FLYWAY_BASELINE_ON_MIGRATE=true`; for a local Maven run against a
pre-Flyway database, use the same environment variable after verifying the
schema and taking a backup. Keep it disabled for production unless the target
database has been reviewed and an explicit baseline is part of the rollout.
Flyway's `flyway_schema_history` table records applied versions and checksums;
applied migration files must not be edited.

No application startup backfill runner is used. Legacy technology data is
migrated once by Flyway rather than reprocessed on every application startup.

## Running Tests

```bash
./mvnw test -Dtest="SoftwareEngineerMapperTest,SoftwareEngineerServiceTest,SoftwareEngineerControllerTest"
```

The database integration tests use PostgreSQL Testcontainers and require a
Docker-compatible runtime:

```bash
./mvnw -Dtest="FlywayMigrationTest,SoftwareEngineerRepositoryTest,ApplicationTests" test
```

These tests do not use H2. `ApplicationTests` loads the full Spring context
against an isolated PostgreSQL container, while `SoftwareEngineerRepositoryTest`
is a focused `@DataJpaTest`.
