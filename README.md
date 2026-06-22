# Splitzone Service

Spring Boot service backed by Oracle Database and Liquibase.

## Local setup

- `dev` profile uses a `splitzone` Oracle user/schema
- `test` profile uses a separate `splitzone_test` Oracle user/schema
- both point at the same local Oracle container and PDB

## Start Oracle with Podman

1. Copy `.env.oracle.example` to `.env.oracle`.
2. Set the system password, PDB name, and app/test user passwords in `.env.oracle`.
3. Start Oracle:

```bash
podman compose --env-file .env.oracle -f compose.dev.yaml up -d
```

On first startup, Oracle will:

- create the PDB from `ORACLE_DATABASE`
- create the dev user from `APP_USER` and `APP_USER_PASSWORD`
- create the test user from `TEST_APP_USER` and `TEST_APP_USER_PASSWORD`

The user creation is handled by [container/oracle/initdb/001-create-app-users.sh](./container/oracle/initdb/001-create-app-users.sh), which is mounted into Oracle's one-time init directory.

If you change `.env.oracle` after the volume already exists, recreate the volume so Oracle runs initialization again:

```bash
podman compose -f compose.dev.yaml down -v
podman compose --env-file .env.oracle -f compose.dev.yaml up -d
```

## Create local property files

These files are gitignored:

- `src/main/resources/application-dev.properties`
- `src/test/resources/application-test.properties`
- `.env.oracle`

Create them from the committed samples:

```bash
cp src/main/resources/application-dev.sample.properties src/main/resources/application-dev.properties
cp src/test/resources/application-test.sample.properties src/test/resources/application-test.properties
cp .env.oracle.example .env.oracle
```

If you change `ORACLE_DATABASE` in `.env.oracle`, update the JDBC service name in both copied property files.

## Run the application

Start the service with the dev profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Liquibase runs automatically on startup using `src/main/resources/db/changelog/db.changelog-master.yaml`.

## Database-backed tests

If you add repository or integration tests, use the `test` profile and point it at a separate test schema in the same Oracle instance.

Example:

```kotlin
@ActiveProfiles("test")
@SpringBootTest
class RepositoryIntegrationTest
```

You do not need to create the dev or test users manually if you start from a fresh Oracle volume.

## Notes

- Oracle container definition: [compose.dev.yaml](./compose.dev.yaml)
- Oracle init scripts: [container/oracle/initdb](./container/oracle/initdb)
- Base application config: [src/main/resources/application.properties](./src/main/resources/application.properties)
- Liquibase changelog: [src/main/resources/db/changelog/db.changelog-master.yaml](./src/main/resources/db/changelog/db.changelog-master.yaml)
