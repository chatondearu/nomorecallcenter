#!/usr/bin/env bash
# Postgres + Nuxt dev API + admin for cda-design-loop (no Docker API on :3000).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

API_PORT="${API_PORT:-3000}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
DATABASE_URL="${DATABASE_URL:-postgresql://nmcc:nmcc@localhost:${POSTGRES_PORT}/nmcc}"
ADMIN_PORT="${ADMIN_PORT:-3001}"
API_PROXY_TARGET="${API_PROXY_TARGET:-http://127.0.0.1:${API_PORT}}"

API_PID=""
ADMIN_PID=""

cleanup() {
  [[ -n "$API_PID" ]] && kill "$API_PID" 2>/dev/null || true
  [[ -n "$ADMIN_PID" ]] && kill "$ADMIN_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

echo "[dev:design] Postgres only (host port ${POSTGRES_PORT})"
if docker compose ps api 2>/dev/null | grep -q 'Up'; then
  echo "[dev:design] stopping compose api so Nuxt dev can bind :${API_PORT}"
  docker compose stop api
fi

docker compose up -d postgres

echo "[dev:design] waiting for Postgres"
for i in $(seq 1 30); do
  if docker compose exec -T postgres pg_isready -U "${POSTGRES_USER:-nmcc}" -d "${POSTGRES_DB:-nmcc}" >/dev/null 2>&1; then
    break
  fi
  if [[ "$i" -eq 30 ]]; then
    echo "[dev:design] Postgres not ready"
    exit 1
  fi
  sleep 1
done

echo "[dev:design] migrate"
DATABASE_URL="$DATABASE_URL" pnpm --filter @nmcc/db drizzle:migrate

if ! curl -fsS "http://127.0.0.1:${API_PORT}/api/health" >/dev/null 2>&1; then
  echo "[dev:design] starting @nmcc/app on :${API_PORT}"
  DATABASE_URL="$DATABASE_URL" BETTER_AUTH_URL="${BETTER_AUTH_URL:-http://127.0.0.1:${API_PORT}}" \
    pnpm --filter @nmcc/app dev --host 127.0.0.1 --port "$API_PORT" &
  API_PID=$!
else
  echo "[dev:design] API already listening on :${API_PORT}"
fi

if ! curl -fsS "http://127.0.0.1:${ADMIN_PORT}/login" >/dev/null 2>&1; then
  echo "[dev:design] starting @nmcc/admin on :${ADMIN_PORT}"
  API_PROXY_TARGET="$API_PROXY_TARGET" pnpm --filter @nmcc/admin dev --host 127.0.0.1 --port "$ADMIN_PORT" &
  ADMIN_PID=$!
else
  echo "[dev:design] admin already listening on :${ADMIN_PORT}"
fi

echo "[dev:design] waiting for HTTP"
for i in $(seq 1 90); do
  api_ok=0
  admin_ok=0
  curl -fsS "http://127.0.0.1:${API_PORT}/api/health" | grep -q '"ok":true' && api_ok=1
  curl -fsS "http://127.0.0.1:${ADMIN_PORT}/login" >/dev/null 2>&1 && admin_ok=1
  if [[ "$api_ok" -eq 1 && "$admin_ok" -eq 1 ]]; then
    echo "[dev:design] ready"
    echo "  DEV_URL_SITE=http://127.0.0.1:${API_PORT}"
    echo "  DEV_URL_ADMIN=http://127.0.0.1:${ADMIN_PORT}"
    echo "  routes: design/routes.json"
    echo "  auth:   pnpm auth:acceptance (once) → pnpm design:auth"
    echo "  shots:  pnpm design:capture"
    echo "[dev:design] Ctrl+C to stop Nuxt processes (Postgres stays up)"
    wait "$API_PID" "$ADMIN_PID" 2>/dev/null || wait
    exit 0
  fi
  sleep 2
done

echo "[dev:design] timeout waiting for dev servers"
exit 1
