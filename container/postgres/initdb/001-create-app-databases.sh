#!/bin/sh
set -eu

if [ -z "${APP_DB:-}" ] || [ -z "${APP_USER:-}" ] || [ -z "${APP_USER_PASSWORD:-}" ]; then
  echo "APP_DB, APP_USER, and APP_USER_PASSWORD must be set"
  exit 1
fi

if [ -z "${TEST_APP_DB:-}" ] || [ -z "${TEST_APP_USER:-}" ] || [ -z "${TEST_APP_USER_PASSWORD:-}" ]; then
  echo "TEST_APP_DB, TEST_APP_USER, and TEST_APP_USER_PASSWORD must be set"
  exit 1
fi

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<EOSQL
SELECT 'CREATE USER "${APP_USER}" WITH PASSWORD ''${APP_USER_PASSWORD}'''
WHERE NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '${APP_USER}');
\gexec

SELECT 'CREATE DATABASE "${APP_DB}" OWNER "${APP_USER}"'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${APP_DB}');
\gexec

SELECT 'CREATE USER "${TEST_APP_USER}" WITH PASSWORD ''${TEST_APP_USER_PASSWORD}'''
WHERE NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '${TEST_APP_USER}');
\gexec

SELECT 'CREATE DATABASE "${TEST_APP_DB}" OWNER "${TEST_APP_USER}"'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${TEST_APP_DB}');
\gexec
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$APP_DB" <<EOSQL
ALTER SCHEMA public OWNER TO "${APP_USER}";
GRANT ALL ON SCHEMA public TO "${APP_USER}";
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$TEST_APP_DB" <<EOSQL
ALTER SCHEMA public OWNER TO "${TEST_APP_USER}";
GRANT ALL ON SCHEMA public TO "${TEST_APP_USER}";
EOSQL
