#!/usr/bin/env bash
# Compose smoke: up → health → migrate
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

API_PORT="${API_PORT:-3000}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
DATABASE_URL="${DATABASE_URL:-postgresql://nmcc:nmcc@localhost:${POSTGRES_PORT}/nmcc}"
HEALTH_URL="http://127.0.0.1:${API_PORT}/api/health"

echo "[smoke] docker compose up -d --build"
docker compose up -d --build

echo "[smoke] waiting for API health at ${HEALTH_URL}"
for i in $(seq 1 60); do
  if curl -fsS "$HEALTH_URL" | grep -q '"ok":true'; then
    echo "[smoke] health OK"
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "[smoke] health check failed"
    docker compose logs api postgres || true
    exit 1
  fi
  sleep 2
done

echo "[smoke] drizzle migrate"
DATABASE_URL="$DATABASE_URL" pnpm --filter @nmcc/db drizzle:migrate

echo "[smoke] done"
