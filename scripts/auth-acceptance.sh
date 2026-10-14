#!/usr/bin/env bash
# Auth acceptance: compose → bootstrap admin → invite → second user → reject open signup
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

API_PORT="${API_PORT:-3000}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
DATABASE_URL="${DATABASE_URL:-postgresql://nmcc:nmcc@localhost:${POSTGRES_PORT}/nmcc}"
BASE_URL="http://127.0.0.1:${API_PORT}"
COOKIE_JAR="$(mktemp)"
trap 'rm -f "$COOKIE_JAR"' EXIT

ADMIN_EMAIL="${AUTH_ACCEPT_ADMIN_EMAIL:-admin@example.com}"
ADMIN_PASSWORD="${AUTH_ACCEPT_ADMIN_PASSWORD:-AdminPass123!}"
USER_EMAIL="${AUTH_ACCEPT_USER_EMAIL:-user@example.com}"
USER_PASSWORD="${AUTH_ACCEPT_USER_PASSWORD:-UserPass123!}"

echo "[auth] wiping compose volumes for a clean instance"
docker compose down -v >/dev/null 2>&1 || true

echo "[auth] docker compose up -d --build"
docker compose up -d --build

echo "[auth] waiting for API health"
for i in $(seq 1 60); do
  if curl -fsS "${BASE_URL}/api/health" | grep -q '"ok":true'; then
    echo "[auth] health OK"
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "[auth] health check failed"
    docker compose logs api postgres || true
    exit 1
  fi
  sleep 2
done

echo "[auth] drizzle migrate"
DATABASE_URL="$DATABASE_URL" pnpm --filter @nmcc/db drizzle:migrate

echo "[auth] register bootstrap admin"
SIGNUP_ADMIN="$(curl -fsS -c "$COOKIE_JAR" -b "$COOKIE_JAR" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/auth/sign-up/email" \
  -d "{\"name\":\"Admin\",\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASSWORD}\"}")"
echo "$SIGNUP_ADMIN" | grep -q "$ADMIN_EMAIL"

echo "[auth] session /api/me as admin"
ME_ADMIN="$(curl -fsS -c "$COOKIE_JAR" -b "$COOKIE_JAR" "${BASE_URL}/api/me")"
echo "$ME_ADMIN" | grep -q '"role":"admin"'

echo "[auth] reject open signup without invite"
HTTP_OPEN="$(curl -sS -o /tmp/nmcc-open-signup.json -w '%{http_code}' \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/auth/sign-up/email" \
  -d '{"name":"Nope","email":"open@example.com","password":"OpenPass123!"}' || true)"
if [[ "$HTTP_OPEN" != "403" && "$HTTP_OPEN" != "400" ]]; then
  echo "[auth] expected forbidden open signup, got HTTP ${HTTP_OPEN}"
  cat /tmp/nmcc-open-signup.json || true
  exit 1
fi
echo "[auth] open signup rejected (HTTP ${HTTP_OPEN})"

echo "[auth] create invite"
INVITE_JSON="$(curl -fsS -c "$COOKIE_JAR" -b "$COOKIE_JAR" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/invites" \
  -d "{\"email\":\"${USER_EMAIL}\"}")"
INVITE_TOKEN="$(node -e 'const j=JSON.parse(process.argv[1]); if (!j.token) process.exit(2); process.stdout.write(j.token)' "$INVITE_JSON")"

COOKIE_USER="$(mktemp)"
trap 'rm -f "$COOKIE_JAR" "$COOKIE_USER"' EXIT

echo "[auth] register invited user"
SIGNUP_USER="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/auth/sign-up/email" \
  -d "{\"name\":\"User\",\"email\":\"${USER_EMAIL}\",\"password\":\"${USER_PASSWORD}\",\"inviteToken\":\"${INVITE_TOKEN}\"}")"
echo "$SIGNUP_USER" | grep -q "$USER_EMAIL"

ME_USER="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" "${BASE_URL}/api/me")"
echo "$ME_USER" | grep -q '"role":"user"'

echo "[auth] reject reused invite"
HTTP_REUSE="$(curl -sS -o /tmp/nmcc-reuse-invite.json -w '%{http_code}' \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/auth/sign-up/email" \
  -d "{\"name\":\"Reuse\",\"email\":\"reuse@example.com\",\"password\":\"ReusePass123!\",\"inviteToken\":\"${INVITE_TOKEN}\"}" || true)"
if [[ "$HTTP_REUSE" != "403" && "$HTTP_REUSE" != "400" ]]; then
  echo "[auth] expected forbidden reused invite, got HTTP ${HTTP_REUSE}"
  cat /tmp/nmcc-reuse-invite.json || true
  exit 1
fi
echo "[auth] reused invite rejected (HTTP ${HTTP_REUSE})"

echo "[auth] done"
