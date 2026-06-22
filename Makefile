SHELL := /bin/sh

ENV_FILE := .env.postgres
COMPOSE_FILE := compose.dev.yaml
MVNW := ./mvnw

.PHONY: help bootstrap db-up db-down db-reset db-logs db-psql run test clean

help:
	@printf "%s\n" \
		"Available targets:" \
		"  make bootstrap  - create local config files from samples if missing" \
		"  make db-up      - start local PostgreSQL with Podman" \
		"  make db-down    - stop local PostgreSQL" \
		"  make db-reset   - stop PostgreSQL and remove its volume" \
		"  make db-logs    - tail PostgreSQL container logs" \
		"  make db-psql    - open psql in the running PostgreSQL container" \
		"  make run        - run the Spring Boot app with dev profile" \
		"  make test       - run the Maven test suite" \
		"  make clean      - run Maven clean"

bootstrap:
	@[ -f .env.postgres ] || cp .env.postgres.example .env.postgres
	@[ -f src/main/resources/application-dev.properties ] || cp src/main/resources/application-dev.sample.properties src/main/resources/application-dev.properties
	@[ -f src/test/resources/application-test.properties ] || cp src/test/resources/application-test.sample.properties src/test/resources/application-test.properties
	@printf "%s\n" "Bootstrap complete. Review .env.postgres and local application-*.properties before first run."

db-up:
	podman compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) up -d

db-down:
	podman compose -f $(COMPOSE_FILE) down

db-reset:
	podman compose -f $(COMPOSE_FILE) down -v

db-logs:
	podman compose -f $(COMPOSE_FILE) logs -f postgres-db

db-psql:
	podman exec -it splitzone-postgres-db psql -U postgres -d postgres

run:
	$(MVNW) spring-boot:run -Dspring-boot.run.profiles=dev

test:
	$(MVNW) test

clean:
	$(MVNW) clean
