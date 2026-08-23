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

**1. Start the database**

```bash
docker-compose up -d
```

**2. Run the application**

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

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
