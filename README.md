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

The package layout follows a ports-and-adapters boundary:

```
src/main/java/com/michaelhope/
├── api/             HTTP controllers, request/response records, error handling
├── domain/          entities and domain exceptions; no Spring imports
├── application/    use-case services, mapping, pagination, outbound ports
└── infrastructure/ JPA/Kafka/Caffeine adapters, configuration, web plumbing
```

The application layer owns orchestration and depends on interfaces in
`application/port/out`. Infrastructure supplies those interfaces. This keeps
HTTP, persistence, messaging, and caching replaceable at the application
boundary without pretending that JPA entities are API DTOs.

## Running the App

For a reviewer, one command starts the app and its dependencies:

```bash
docker compose up --build
```

Compose waits for healthy PostgreSQL and Kafka before starting the application.
The API is available at `http://localhost:8080`. Stop it with `Ctrl-C`, or use
`docker compose down` from another terminal. The PostgreSQL data volume is
retained by default; use `docker compose down -v` when you intentionally want
to remove local data.

For local Maven development, start only the dependencies and run the app on the
host:

```bash
docker compose up -d db kafka
./mvnw spring-boot:run
```

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
docker compose up --build
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
| GET    | `/api/v1/software-engineer?limit=20&offset=0` | List engineers with pagination |
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

The engineer list endpoint uses limit/offset pagination. `limit` defaults to 20
and is capped at 100; `offset` defaults to 0. The response includes `items`,
`limit`, `offset`, `total`, and `hasNext`.

## Engineer response cache

`GET /api/v1/software-engineer/{id}` uses a bounded in-process Caffeine cache of
immutable response DTOs. Updates refresh the entry after commit and deletes evict
it after commit. The default limit is 500 engineers with a 10-minute expiry; both
values are configurable in `application.properties`.

Cache statistics are available at:

```text
GET /api/v1/cache/engineer-by-id/stats
```

With the application and its dependencies running, repeat the same read and view
the hit/miss change with:

```bash
./scripts/load-test-engineer-cache.sh
```

The script accepts `BASE_URL`, `ENGINEER_ID`, `REQUESTS`, and `CONCURRENCY`
environment variables.

## Running Tests

Run the complete suite with:

```bash
./mvnw test
```

The suite is deliberately split by boundary:

- `domain/` tests are plain JUnit unit tests.
- `api/` tests use `@WebMvcTest` and mock application services.
- `infrastructure/persistence/` tests use PostgreSQL Testcontainers, not H2;
  `SoftwareEngineerRepositoryTest` is a focused `@DataJpaTest`.
- `infrastructure/persistence/FlywayMigrationTest` verifies the migration chain.
- `integration/ApplicationTests` loads the full Spring context against an
  isolated PostgreSQL Testcontainer.

Run the fast, non-database tests with:

```bash
./mvnw -Dtest="TechnologyTest,SoftwareEngineerMapperTest,TechnologyServiceTest,SoftwareEngineerServiceTest,SoftwareEngineerControllerTest" test
```

The integration tests require a Docker-compatible runtime:

```bash
./mvnw -Dtest="FlywayMigrationTest,SoftwareEngineerRepositoryTest,ApplicationTests" test
```

## Design Decisions and Trade-offs

- PostgreSQL is the only supported runtime database. Flyway owns schema changes
  and Hibernate is validation-only, which makes deployments explicit but adds
  migration maintenance.
- Limit/offset pagination is simple and matches the existing API, but deep
  offsets become less efficient than cursor pagination on very large tables.
- The engineer-by-ID cache is bounded Caffeine in each application instance.
  It reduces repeated reads, but it is not a distributed cache; horizontal
  deployments require cache invalidation or a shared cache strategy.
- Kafka audit history is asynchronous. CRUD requests stay responsive, but the
  history endpoint is eventually consistent and depends on broker availability.
- JPA entities remain persistence-aware because this is a small service and the
  schema mapping is straightforward. They are never returned directly from
  controllers; API response records are mapped at the application boundary.

See [DECISIONS.md](DECISIONS.md) for the numbered rationale behind these and
other choices.
