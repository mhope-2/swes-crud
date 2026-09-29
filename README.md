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

The application uses Hibernate `update` for this learning project. On startup,
`TechnologyBackfillRunner` migrates values from a legacy `tech_stack` column
when present. The legacy column is intentionally retained until a versioned
production migration is introduced.

**Request body (POST / PUT):**

```json
{
  "name": "Alice",
  "technologies": ["Java", "Spring", "PostgreSQL"]
}
```

Technology names are trimmed, normalized case-insensitively, deduplicated, and returned in alphabetical order. PostgreSQL stores reusable `technology` rows and a `software_engineer_technology` join table.

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

```bash
./mvnw test -Dtest="SoftwareEngineerMapperTest,SoftwareEngineerServiceTest,SoftwareEngineerControllerTest"
```

> `ApplicationTests` (context load) requires the Docker database to be running.
