# Spring Boot REST API — Learning Project

A simple CRUD REST API built with Spring Boot for personal learning. Manages a list of software engineers stored in PostgreSQL.

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
├── model/         JPA entity
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

CRUD mutations publish versioned events to `software-engineer.events.v1`, keyed
by engineer ID. The audit consumer stores them in the `engineer_audit` table.
History is eventually consistent with the CRUD response because Kafka
processing happens asynchronously.

**Request body (POST / PUT):**

```json
{
  "name": "Alice",
  "techStack": "Java"
}
```

## Running Tests

```bash
./mvnw test -Dtest="SoftwareEngineerMapperTest,SoftwareEngineerServiceTest,SoftwareEngineerControllerTest"
```

> `ApplicationTests` (context load) requires the Docker database to be running.
