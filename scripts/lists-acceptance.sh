#!/usr/bin/env bash
# Lists acceptance: compose → migrate → auth member → ARCEP seed → authenticated sync
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

echo "[lists] wiping compose volumes for a clean instance"
docker compose down -v >/dev/null 2>&1 || true

echo "[lists] docker compose up -d --build"
docker compose up -d --build

echo "[lists] waiting for API health"
for i in $(seq 1 60); do
  if curl -fsS "${BASE_URL}/api/health" | grep -q '"ok":true'; then
    echo "[lists] health OK"
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "[lists] health check failed"
    docker compose logs api postgres || true
    exit 1
  fi
  sleep 2
done

echo "[lists] drizzle migrate"
DATABASE_URL="$DATABASE_URL" pnpm --filter @nmcc/db drizzle:migrate

echo "[lists] register bootstrap admin (member session)"
curl -fsS -c "$COOKIE_JAR" -b "$COOKIE_JAR" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/auth/sign-up/email" \
  -d "{\"name\":\"Admin\",\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASSWORD}\"}" \
  | grep -q "$ADMIN_EMAIL"

echo "[lists] reject unauthenticated sync"
HTTP_UNAUTH="$(curl -sS -o /tmp/nmcc-lists-unauth.json -w '%{http_code}' \
  "${BASE_URL}/api/lists/sync" || true)"
if [[ "$HTTP_UNAUTH" != "401" ]]; then
  echo "[lists] expected 401 for unauthenticated sync, got HTTP ${HTTP_UNAUTH}"
  cat /tmp/nmcc-lists-unauth.json || true
  exit 1
fi
echo "[lists] unauthenticated sync rejected (HTTP 401)"

echo "[lists] seed ARCEP demarchage prefixes"
# Prefer live MAJNUM; fall back to fixture if fetch fails (offline CI)
if ! DATABASE_URL="$DATABASE_URL" pnpm --filter @nmcc/db lists:seed; then
  echo "[lists] live seed failed — using fixture"
  DATABASE_URL="$DATABASE_URL" pnpm --filter @nmcc/db lists:seed -- --fixture
fi

echo "[lists] authenticated sync"
SYNC_JSON="$(curl -fsS -c "$COOKIE_JAR" -b "$COOKIE_JAR" \
  "${BASE_URL}/api/lists/sync?since=0")"

echo "$SYNC_JSON" | node -e '
const j = JSON.parse(require("fs").readFileSync(0, "utf8"));
if (!Number.isFinite(j.revision) || j.revision < 1) {
  console.error("expected revision >= 1", j);
  process.exit(1);
}
if (!Array.isArray(j.entries) || j.entries.length < 1) {
  console.error("expected >= 1 entries", j);
  process.exit(1);
}
const hit = j.entries.find((e) => typeof e.pattern === "string" && e.pattern.startsWith("0892") && e.active === true);
if (!hit) {
  console.error("expected active 0892* demarchage prefix", j.entries.slice(0, 5));
  process.exit(1);
}
if (!hit.attribution || !String(hit.attribution).includes("Licence Ouverte")) {
  console.error("expected LO 2.0 attribution", hit);
  process.exit(1);
}
if (!hit.sourceDate) {
  console.error("expected sourceDate", hit);
  process.exit(1);
}
console.log(`[lists] sync OK revision=${j.revision} entries=${j.entries.length} sample=${hit.pattern}`);
'

echo "[lists] done"
