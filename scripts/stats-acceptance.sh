#!/usr/bin/env bash
# Stats acceptance (Slice 4): two users ingest → personal vs instance differ; non-admin cannot read live feed
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

API_PORT="${API_PORT:-3017}"
POSTGRES_PORT="${POSTGRES_PORT:-5437}"
DATABASE_URL="${DATABASE_URL:-postgresql://nmcc:nmcc@localhost:${POSTGRES_PORT}/nmcc}"
BASE_URL="http://127.0.0.1:${API_PORT}"
COOKIE_ADMIN="$(mktemp)"
COOKIE_USER="$(mktemp)"
trap 'rm -f "$COOKIE_ADMIN" "$COOKIE_USER"' EXIT

ADMIN_EMAIL="${AUTH_ACCEPT_ADMIN_EMAIL:-admin@example.com}"
ADMIN_PASSWORD="${AUTH_ACCEPT_ADMIN_PASSWORD:-AdminPass123!}"
USER_EMAIL="${AUTH_ACCEPT_USER_EMAIL:-user@example.com}"
USER_PASSWORD="${AUTH_ACCEPT_USER_PASSWORD:-UserPass123!}"
HASH_A="$(printf 'a%.0s' {1..64})"
HASH_B="$(printf 'b%.0s' {1..64})"

export API_PORT POSTGRES_PORT
export BETTER_AUTH_URL="${BETTER_AUTH_URL:-http://localhost:${API_PORT}}"
export EVENTS_RETENTION_DAYS="${EVENTS_RETENTION_DAYS:-90}"

echo "[stats] wiping compose volumes for a clean instance"
docker compose down -v >/dev/null 2>&1 || true

echo "[stats] docker compose up -d --build (API_PORT=${API_PORT} POSTGRES_PORT=${POSTGRES_PORT})"
docker compose up -d --build

echo "[stats] waiting for API health"
for i in $(seq 1 60); do
  if curl -fsS "${BASE_URL}/api/health" | grep -q '"ok":true'; then
    echo "[stats] health OK"
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "[stats] health check failed"
    docker compose logs api postgres || true
    exit 1
  fi
  sleep 2
done

echo "[stats] drizzle migrate"
DATABASE_URL="$DATABASE_URL" pnpm --filter @nmcc/db drizzle:migrate

echo "[stats] register bootstrap admin"
curl -fsS -c "$COOKIE_ADMIN" -b "$COOKIE_ADMIN" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/auth/sign-up/email" \
  -d "{\"name\":\"Admin\",\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASSWORD}\"}" \
  | grep -q "$ADMIN_EMAIL"

echo "[stats] create invite + register member"
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

echo "[stats] reject clear full number"
HTTP_CLEAR="$(curl -sS -o /tmp/nmcc-stats-clear.json -w '%{http_code}' \
  -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/events" \
  -d '{"prefix":"0612345678","action":"blocked"}' || true)"
if [[ "$HTTP_CLEAR" != "400" ]]; then
  echo "[stats] expected 400 for clear number prefix, got HTTP ${HTTP_CLEAR}"
  cat /tmp/nmcc-stats-clear.json || true
  exit 1
fi
echo "[stats] clear full number rejected (HTTP 400)"

echo "[stats] member ingests blocked + allowed"
curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/events" \
  -d "{\"prefix\":\"0892\",\"hash\":\"${HASH_A}\",\"action\":\"blocked\"}" \
  | grep -q '"action":"blocked"'

curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/events" \
  -d "{\"prefix\":\"0899\",\"action\":\"allowed\"}" \
  | grep -q '"action":"allowed"'

echo "[stats] admin ingests screened"
curl -fsS -c "$COOKIE_ADMIN" -b "$COOKIE_ADMIN" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/events" \
  -d "{\"prefix\":\"0892\",\"hash\":\"${HASH_B}\",\"action\":\"screened\"}" \
  | grep -q '"action":"screened"'

echo "[stats] personal stats for member (blocked=1, allowed=1, no admin screened)"
ME_JSON="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" "${BASE_URL}/api/stats/me")"
node -e '
const j = JSON.parse(process.argv[1]);
const c = j.counters?.["7d"];
if (!c || c.blocked !== 1 || c.allowed !== 1 || c.screened !== 0) {
  console.error("personal counters mismatch", j); process.exit(2);
}
if (!Array.isArray(j.topPrefixes) || !j.topPrefixes.some((p) => p.prefix === "0892")) {
  console.error("expected top prefix 0892", j); process.exit(2);
}
if (!Array.isArray(j.recent) || j.recent.length < 2) {
  console.error("expected personal recent history", j); process.exit(2);
}
if (j.recent.some((e) => e.userId)) {
  console.error("personal recent must not expose userId", j); process.exit(2);
}
console.log("[stats] personal OK", c);
' "$ME_JSON"

echo "[stats] instance aggregates include both users"
INST_JSON="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" "${BASE_URL}/api/stats/instance")"
node -e '
const j = JSON.parse(process.argv[1]);
const c = j.counters?.["7d"];
if (!c || c.blocked !== 1 || c.allowed !== 1 || c.screened !== 1) {
  console.error("instance counters mismatch (personal vs instance must differ)", j); process.exit(2);
}
if (j.proposalFunnel != null || j.eventVolume != null) {
  console.error("member must not get admin extras", j); process.exit(2);
}
if ((j.recent || []).some((e) => e.userId)) {
  console.error("instance recent must omit userId for members", j); process.exit(2);
}
console.log("[stats] instance (member) OK", c);
' "$INST_JSON"

echo "[stats] admin instance extras"
ADMIN_INST="$(curl -fsS -c "$COOKIE_ADMIN" -b "$COOKIE_ADMIN" "${BASE_URL}/api/stats/instance")"
node -e '
const j = JSON.parse(process.argv[1]);
if (!j.proposalFunnel || typeof j.eventVolume !== "number" || j.eventVolume < 3) {
  console.error("admin extras missing", j); process.exit(2);
}
console.log("[stats] admin instance OK volume=" + j.eventVolume);
' "$ADMIN_INST"

echo "[stats] member cannot read live feed"
HTTP_FEED="$(curl -sS -o /tmp/nmcc-stats-feed.json -w '%{http_code}' \
  -c "$COOKIE_USER" -b "$COOKIE_USER" \
  "${BASE_URL}/api/admin/events/feed" || true)"
if [[ "$HTTP_FEED" != "403" ]]; then
  echo "[stats] expected 403 for member feed, got HTTP ${HTTP_FEED}"
  cat /tmp/nmcc-stats-feed.json || true
  exit 1
fi
echo "[stats] member feed rejected (HTTP 403)"

echo "[stats] admin live feed"
FEED_JSON="$(curl -fsS -c "$COOKIE_ADMIN" -b "$COOKIE_ADMIN" "${BASE_URL}/api/admin/events/feed?limit=10")"
node -e '
const j = JSON.parse(process.argv[1]);
if (!Array.isArray(j.events) || j.events.length < 3) {
  console.error("expected >=3 feed events", j); process.exit(2);
}
const e = j.events[0];
if (!e.action || !e.userId || !e.occurredAt) {
  console.error("feed event shape", e); process.exit(2);
}
if (e.prefix == null && e.hash == null) {
  console.error("feed needs prefix or hash", e); process.exit(2);
}
console.log("[stats] admin feed OK count=" + j.events.length);
' "$FEED_JSON"

echo "[stats] done"
