#!/usr/bin/env bash
# Admin UI acceptance (Slice 6): member proposes → admin approves via same APIs the UI calls (no SQL).
# Documents UI path: http://127.0.0.1:${ADMIN_PORT}/login → Propositions → Approuver.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

API_PORT="${API_PORT:-3010}"
ADMIN_PORT="${ADMIN_PORT:-3011}"
POSTGRES_PORT="${POSTGRES_PORT:-5434}"
DATABASE_URL="${DATABASE_URL:-postgresql://nmcc:nmcc@localhost:${POSTGRES_PORT}/nmcc}"
BASE_URL="http://127.0.0.1:${API_PORT}"
ADMIN_URL="http://127.0.0.1:${ADMIN_PORT}"
COOKIE_ADMIN="$(mktemp)"
COOKIE_USER="$(mktemp)"
ADMIN_PID=""
trap 'rm -f "$COOKIE_ADMIN" "$COOKIE_USER"; [[ -n "$ADMIN_PID" ]] && kill "$ADMIN_PID" 2>/dev/null || true' EXIT

ADMIN_EMAIL="${AUTH_ACCEPT_ADMIN_EMAIL:-admin@example.com}"
ADMIN_PASSWORD="${AUTH_ACCEPT_ADMIN_PASSWORD:-AdminPass123!}"
USER_EMAIL="${AUTH_ACCEPT_USER_EMAIL:-user@example.com}"
USER_PASSWORD="${AUTH_ACCEPT_USER_PASSWORD:-UserPass123!}"
PATTERN="${ADMIN_ACCEPT_PATTERN:-0899123}"

export API_PORT POSTGRES_PORT
export BETTER_AUTH_URL="${BETTER_AUTH_URL:-http://localhost:${API_PORT}}"
export API_PROXY_TARGET="${API_PROXY_TARGET:-http://127.0.0.1:${API_PORT}}"
export ADMIN_URL="${ADMIN_URL:-http://127.0.0.1:${ADMIN_PORT}}"

echo "[admin] wiping compose volumes for a clean instance"
docker compose down -v >/dev/null 2>&1 || true

echo "[admin] docker compose up -d --build (API_PORT=${API_PORT} POSTGRES_PORT=${POSTGRES_PORT})"
docker compose up -d --build

echo "[admin] waiting for API health"
for i in $(seq 1 60); do
  if curl -fsS "${BASE_URL}/api/health" | grep -q '"ok":true'; then
    echo "[admin] health OK"
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "[admin] health check failed"
    docker compose logs api postgres || true
    exit 1
  fi
  sleep 2
done

echo "[admin] drizzle migrate"
DATABASE_URL="$DATABASE_URL" pnpm --filter @nmcc/db drizzle:migrate

echo "[admin] register bootstrap admin"
curl -fsS -c "$COOKIE_ADMIN" -b "$COOKIE_ADMIN" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/auth/sign-up/email" \
  -d "{\"name\":\"Admin\",\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASSWORD}\"}" \
  | grep -q "$ADMIN_EMAIL"

echo "[admin] invite + register member"
INVITE_JSON="$(curl -fsS -c "$COOKIE_ADMIN" -b "$COOKIE_ADMIN" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/invites" \
  -d "{\"email\":\"${USER_EMAIL}\"}")"
INVITE_TOKEN="$(node -e 'const j=JSON.parse(process.argv[1]); if (!j.token) process.exit(2); process.stdout.write(j.token)' "$INVITE_JSON")"

curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/auth/sign-up/email" \
  -d "{\"name\":\"User\",\"email\":\"${USER_EMAIL}\",\"password\":\"${USER_PASSWORD}\",\"inviteToken\":\"${INVITE_TOKEN}\"}" \
  | grep -q "$USER_EMAIL"

echo "[admin] member proposes pattern ${PATTERN}"
PROP_JSON="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/proposals" \
  -d "{\"pattern\":\"${PATTERN}\",\"matchType\":\"prefix\"}")"
PROP_ID="$(node -e '
const j = JSON.parse(process.argv[1]);
if (!j.proposal?.id || j.proposal.status !== "pending") { console.error(j); process.exit(2); }
process.stdout.write(j.proposal.id);
' "$PROP_JSON")"
echo "[admin] proposal id=${PROP_ID}"

echo "[admin] starting Nuxt admin on ${ADMIN_PORT} (proxy → ${API_PROXY_TARGET})"
API_PROXY_TARGET="$API_PROXY_TARGET" pnpm --filter @nmcc/admin exec nuxt dev --port "$ADMIN_PORT" --host 127.0.0.1 >/tmp/nmcc-admin-accept.log 2>&1 &
ADMIN_PID=$!

echo "[admin] waiting for admin UI + /api proxy"
for i in $(seq 1 90); do
  if curl -fsS "${ADMIN_URL}/login" >/dev/null 2>&1 \
    && curl -fsS "${ADMIN_URL}/api/health" | grep -q '"ok":true'; then
    echo "[admin] UI + proxy OK"
    break
  fi
  if [[ "$i" -eq 90 ]]; then
    echo "[admin] admin UI failed to start"
    cat /tmp/nmcc-admin-accept.log || true
    exit 1
  fi
  sleep 2
done

echo "[admin] sign-in via admin proxy (same path as UI)"
COOKIE_UI="$(mktemp)"
trap 'rm -f "$COOKIE_ADMIN" "$COOKIE_USER" "$COOKIE_UI"; [[ -n "$ADMIN_PID" ]] && kill "$ADMIN_PID" 2>/dev/null || true' EXIT

curl -fsS -c "$COOKIE_UI" -b "$COOKIE_UI" \
  -H 'content-type: application/json' \
  -H "origin: ${ADMIN_URL}" \
  -X POST "${ADMIN_URL}/api/auth/sign-in/email" \
  -d "{\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASSWORD}\"}" \
  | grep -q "$ADMIN_EMAIL"

ME_JSON="$(curl -fsS -c "$COOKIE_UI" -b "$COOKIE_UI" "${ADMIN_URL}/api/me")"
echo "$ME_JSON" | grep -q '"role":"admin"'

echo "[admin] list pending via admin proxy"
LIST_JSON="$(curl -fsS -c "$COOKIE_UI" -b "$COOKIE_UI" "${ADMIN_URL}/api/proposals?status=pending")"
node -e '
const j = JSON.parse(process.argv[1]);
const id = process.argv[2];
if (!Array.isArray(j.proposals) || !j.proposals.some(p => p.id === id)) {
  console.error("proposal missing from queue", j); process.exit(2);
}
' "$LIST_JSON" "$PROP_ID"

echo "[admin] approve via admin proxy (UI Approuver)"
curl -fsS -c "$COOKIE_UI" -b "$COOKIE_UI" \
  -X POST "${ADMIN_URL}/api/proposals/${PROP_ID}/approve" \
  | grep -q '"approved"'

echo "[admin] shared list contains pattern"
SYNC_JSON="$(curl -fsS -c "$COOKIE_ADMIN" -b "$COOKIE_ADMIN" "${BASE_URL}/api/lists/sync?since=0")"
node -e '
const j = JSON.parse(process.argv[1]);
const pattern = process.argv[2];
if (!j.entries?.some(e => e.pattern === pattern && e.active === true)) {
  console.error("pattern not on shared list", j); process.exit(2);
}
' "$SYNC_JSON" "$PATTERN"

echo "[admin] instance stats + live feed via proxy"
STATS_JSON="$(curl -fsS -c "$COOKIE_UI" -b "$COOKIE_UI" "${ADMIN_URL}/api/stats/instance")"
node -e '
const j = JSON.parse(process.argv[1]);
if (!j.proposalFunnel || typeof j.eventVolume !== "number") {
  console.error("admin extras missing", j); process.exit(2);
}
' "$STATS_JSON"
curl -fsS -c "$COOKIE_UI" -b "$COOKIE_UI" "${ADMIN_URL}/api/admin/events/feed?limit=5" | grep -q '"events"'

echo "[admin] acceptance OK"
echo "[admin] UI path: ${ADMIN_URL}/login → Propositions → Approuver (no SQL)"
