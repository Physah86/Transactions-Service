#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-local}"

# Ensure the Maven wrapper is executable (some clones drop the bit)
chmod +x ./mvnw 2>/dev/null || true

case "$MODE" in
  local)
    echo "▶ Starting with H2 (in-memory). App on http://localhost:8080"
    ./mvnw spring-boot:run
    ;;
  docker)
    echo "▶ Starting with PostgreSQL via Docker Compose. App on http://localhost:8080"
    docker compose up --build
    ;;
  test)
    echo "▶ Running test suite..."
    ./mvnw test
    ;;
  *)
    echo "Usage: ./run.sh [local|docker|test]"
    echo "  local  (default) - run with in-memory H2, needs only Java"
    echo "  docker           - run with PostgreSQL via Docker Compose"
    echo "  test             - run unit + integration tests"
    exit 1
    ;;
esac