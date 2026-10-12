# Architecture Decisions

## 1. Use a ports-and-adapters package layout

Application services depend on outbound interfaces in
`application/port/out`; JPA, Kafka, Caffeine, and servlet implementations live
under `infrastructure/`. This makes the use cases testable without loading a
database or broker and keeps framework-specific code at the edges.

The trade-off is more files and adapter boilerplate than a direct
controller-service-repository arrangement. That cost is worthwhile here because
the project is explicitly being used to practice production service design.

## 2. Keep HTTP records in `api/`

Request validation and response shapes are API concerns. Controllers do not
return JPA entities, so persistence changes do not automatically become API
changes and lazy associations cannot be triggered by JSON serialization.

## 3. Keep domain entities free of Spring imports

The domain package contains the engineer, technology, and audit entities plus
domain exceptions. It does not depend on Spring annotations or services. JPA
mapping annotations remain because these entities are also the persistence
model; a future fully framework-free domain model would require separate JPA
records and a deliberate mapping layer.

## 4. Use Flyway with `ddl-auto=validate`

Schema evolution is versioned and reviewable in SQL migrations. Hibernate still
checks that mappings match the deployed schema, but it cannot silently mutate
it. This adds migration files and rollout discipline in exchange for safer
repeatable deployments.

## 5. Use limit/offset pagination

The API exposes a standard `limit` (default 20, maximum 100) and `offset`
(default 0). The application port receives a framework-neutral page query; the
JPA adapter translates it to Spring Data's `Pageable`. This keeps pagination
policy out of the controller and Spring Data types out of the application core.

Limit/offset is easy to consume and adequate for this service, but cursor
pagination would be preferable for very large, frequently changing datasets.

## 6. Cache immutable engineer response records

The Caffeine cache stores API response records for engineer-by-ID reads, not
mutable JPA entities. It has a maximum size and write expiry, refreshes after a
successful update, and evicts after a successful delete. Statistics are exposed
for the load-test workflow.

This is intentionally an in-process cache. It is operationally simple and
appropriate for a single instance, but it is not a cross-instance consistency
mechanism.

## 7. Test at the boundaries

Domain tests are fast unit tests, API tests are `@WebMvcTest` slices, and
persistence tests use real PostgreSQL Testcontainers. A full-context test is
kept for wiring confidence, not as a substitute for focused tests. Avoiding H2
means the repository tests exercise PostgreSQL and Flyway behavior that matches
production more closely.

## 8. Start the reviewer workflow with Compose

`docker compose up --build` starts PostgreSQL, Kafka, and the application in
one command. Health checks prevent the application from racing its
dependencies. The trade-off is that a local review needs Docker-compatible
runtime resources, but the setup is reproducible and requires no host-installed
database or broker.
