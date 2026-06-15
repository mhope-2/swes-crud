.PHONY: up up-build down logs db run test build clean

# ── Docker ────────────────────────────────────────────────────────────────────

up:
	docker compose up -d

up-build:
	docker compose up -d --build

down:
	docker compose down

logs:
	docker compose logs -f

db:
	docker compose up -d db

# ── Maven ─────────────────────────────────────────────────────────────────────

run:
	./mvnw spring-boot:run

test:
	./mvnw test

build:
	./mvnw package -DskipTests

clean:
	./mvnw clean
