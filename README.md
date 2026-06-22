# Splitzone Service

Spring Boot service backed by PostgreSQL and Liquibase.

## Local setup

- `dev` profile uses the `splitzone` database and user
- `test` profile uses the `splitzone_test` database and user

## Start PostgreSQL with Podman

1. Copy `.env.postgres.example` to `.env.postgres`.
2. Set the superuser password plus app and test database credentials in `.env.postgres`.
3. Start PostgreSQL:

```bash
podman compose --env-file .env.postgres -f compose.dev.yaml up -d
```

You can use the Makefile instead:

```bash
make bootstrap
make db-up
```

On first startup, PostgreSQL will:

- create the app database from `APP_DB`
- create the app user from `APP_USER` and `APP_USER_PASSWORD`
- create the test database from `TEST_APP_DB`
- create the test user from `TEST_APP_USER` and `TEST_APP_USER_PASSWORD`

That initialization is handled by [container/postgres/initdb/001-create-app-databases.sh](./container/postgres/initdb/001-create-app-databases.sh), mounted into `/docker-entrypoint-initdb.d`.

If you previously ran the Oracle container, or if you change `.env.postgres` after the volume already exists, recreate the local database volume:

```bash
podman compose -f compose.dev.yaml down -v
podman compose --env-file .env.postgres -f compose.dev.yaml up -d
```

## Create local property files

These files are gitignored:

- `src/main/resources/application-dev.properties`
- `src/test/resources/application-test.properties`
- `.env.postgres`

Create them from the committed samples:

```bash
cp src/main/resources/application-dev.sample.properties src/main/resources/application-dev.properties
cp src/test/resources/application-test.sample.properties src/test/resources/application-test.properties
cp .env.postgres.example .env.postgres
```

If you change database names in `.env.postgres`, update the JDBC URLs in the copied property files too.

## Run the application

Start the service with the dev profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Or:

```bash
make run
```

Liquibase runs automatically on startup using [db.changelog-master.yaml](./src/main/resources/db/changelog/db.changelog-master.yaml).

## Database-backed tests

Repository or integration tests should use the `test` profile and connect to the `splitzone_test` database.

Example:

```kotlin
@ActiveProfiles("test")
@SpringBootTest
class RepositoryIntegrationTest
```

To run the current test suite:

```bash
make test
```
