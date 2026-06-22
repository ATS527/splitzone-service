#!/bin/sh
set -eu

TARGET_PDB="${ORACLE_DATABASE:-FREEPDB1}"

if [ -z "${APP_USER:-}" ] || [ -z "${APP_USER_PASSWORD:-}" ]; then
  echo "APP_USER and APP_USER_PASSWORD must be set"
  exit 1
fi

if [ -z "${TEST_APP_USER:-}" ] || [ -z "${TEST_APP_USER_PASSWORD:-}" ]; then
  echo "TEST_APP_USER and TEST_APP_USER_PASSWORD must be set"
  exit 1
fi

echo "Creating application users in PDB ${TARGET_PDB}"
createAppUser "${APP_USER}" "${APP_USER_PASSWORD}" "${TARGET_PDB}"
createAppUser "${TEST_APP_USER}" "${TEST_APP_USER_PASSWORD}" "${TARGET_PDB}"
