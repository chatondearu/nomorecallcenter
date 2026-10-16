#!/usr/bin/env bash
# Rules acceptance (Slice 3): create 7d → renew → expire path → propose → admin approve → shared list
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

API_PORT="${API_PORT:-3001}"
POSTGRES_PORT="${POSTGRES_PORT:-5433}"
DATABASE_URL="${DATABASE_URL:-postgresql://nmcc:nmcc@localhost:${POSTGRES_PORT}/nmcc}"
BASE_URL="http://127.0.0.1:${API_PORT}"
COOKIE_ADMIN="$(mktemp)"
COOKIE_USER="$(mktemp)"
trap 'rm -f "$COOKIE_ADMIN" "$COOKIE_USER"' EXIT

ADMIN_EMAIL="${AUTH_ACCEPT_ADMIN_EMAIL:-admin@example.com}"
ADMIN_PASSWORD="${AUTH_ACCEPT_ADMIN_PASSWORD:-AdminPass123!}"
USER_EMAIL="${AUTH_ACCEPT_USER_EMAIL:-user@example.com}"
USER_PASSWORD="${AUTH_ACCEPT_USER_PASSWORD:-UserPass123!}"
PATTERN="${RULES_ACCEPT_PATTERN:-0612345678}"

export API_PORT POSTGRES_PORT
export BETTER_AUTH_URL="${BETTER_AUTH_URL:-http://localhost:${API_PORT}}"

echo "[rules] wiping compose volumes for a clean instance"
docker compose down -v >/dev/null 2>&1 || true

echo "[rules] docker compose up -d --build (API_PORT=${API_PORT} POSTGRES_PORT=${POSTGRES_PORT})"
docker compose up -d --build

echo "[rules] waiting for API health"
for i in $(seq 1 60); do
  if curl -fsS "${BASE_URL}/api/health" | grep -q '"ok":true'; then
    echo "[rules] health OK"
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "[rules] health check failed"
    docker compose logs api postgres || true
    exit 1
  fi
  sleep 2
done

echo "[rules] drizzle migrate"
DATABASE_URL="$DATABASE_URL" pnpm --filter @nmcc/db drizzle:migrate

echo "[rules] register bootstrap admin"
curl -fsS -c "$COOKIE_ADMIN" -b "$COOKIE_ADMIN" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/auth/sign-up/email" \
  -d "{\"name\":\"Admin\",\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASSWORD}\"}" \
  | grep -q "$ADMIN_EMAIL"

echo "[rules] create invite + register member"
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

echo "[rules] create 7d personal rule"
CREATE_JSON="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/rules" \
  -d "{\"pattern\":\"${PATTERN}\",\"matchType\":\"exact\",\"ttlPreset\":\"7d\"}")"
RULE_ID="$(node -e '
const j = JSON.parse(process.argv[1]);
if (!j.rule?.id || j.rule.ttlPreset !== "7d" || !j.rule.expiresAt || j.rule.active !== true) {
  console.error(j); process.exit(2);
}
process.stdout.write(j.rule.id);
' "$CREATE_JSON")"
echo "[rules] rule id=${RULE_ID}"

echo "[rules] create allow for the same pattern"
ALLOW_JSON="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/rules" \
  -d "{\"pattern\":\"${PATTERN}\",\"matchType\":\"exact\",\"ttlPreset\":\"24h\",\"effect\":\"allow\"}")"
ALLOW_ID="$(node -e '
const j = JSON.parse(process.argv[1]);
if (!j.rule?.id || j.rule.effect !== "allow" || j.rule.active !== true) {
  console.error(j); process.exit(2);
}
process.stdout.write(j.rule.id);
' "$ALLOW_JSON")"
echo "[rules] allow id=${ALLOW_ID}"

echo "[rules] same allow again is a conflict"
HTTP_DUP="$(curl -sS -o /tmp/nmcc-rules-dup-allow.json -w '%{http_code}' \
  -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/rules" \
  -d "{\"pattern\":\"${PATTERN}\",\"matchType\":\"exact\",\"ttlPreset\":\"24h\",\"effect\":\"allow\"}" || true)"
if [[ "$HTTP_DUP" != "409" ]]; then
  echo "[rules] expected 409 for duplicate allow, got HTTP ${HTTP_DUP}"
  cat /tmp/nmcc-rules-dup-allow.json || true
  exit 1
fi

echo "[rules] second device syncs the allow"
COOKIE_USER2="$(mktemp)"
trap 'rm -f "$COOKIE_ADMIN" "$COOKIE_USER" "$COOKIE_USER2"' EXIT
curl -fsS -c "$COOKIE_USER2" -b "$COOKIE_USER2" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/auth/sign-in/email" \
  -d "{\"email\":\"${USER_EMAIL}\",\"password\":\"${USER_PASSWORD}\"}" >/dev/null
SYNC_ALLOW="$(curl -fsS -c "$COOKIE_USER2" -b "$COOKIE_USER2" "${BASE_URL}/api/rules/sync")"
node -e '
const j = JSON.parse(process.argv[1]);
const id = process.argv[2];
const hit = (j.rules || []).find((r) => r.id === id);
if (!hit || hit.effect !== "allow") {
  console.error("second device missing allow", j); process.exit(2);
}
console.log("[rules] second device sync OK");
' "$SYNC_ALLOW" "$ALLOW_ID"

echo "[rules] non-owner sync does not include the allow"
SYNC_ADMIN="$(curl -fsS -c "$COOKIE_ADMIN" -b "$COOKIE_ADMIN" "${BASE_URL}/api/rules/sync")"
node -e '
const j = JSON.parse(process.argv[1]);
const id = process.argv[2];
if ((j.rules || []).some((r) => r.id === id)) {
  console.error("non-owner can read allow", j); process.exit(2);
}
console.log("[rules] non-owner sync hides allow");
' "$SYNC_ADMIN" "$ALLOW_ID"

echo "[rules] renew 7d (restart TTL from now)"
RENEW_BEFORE="$(node -e 'const j=JSON.parse(process.argv[1]); process.stdout.write(j.rule.expiresAt)' "$CREATE_JSON")"
sleep 1
RENEW_JSON="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/rules/${RULE_ID}/renew" \
  -d '{}')"
node -e '
const j = JSON.parse(process.argv[1]);
const before = process.argv[2];
if (!j.rule?.expiresAt || j.rule.expiresAt <= before) {
  console.error("renew did not extend expiresAt", { before, after: j.rule?.expiresAt });
  process.exit(2);
}
console.log("[rules] renew OK", j.rule.expiresAt);
' "$RENEW_JSON" "$RENEW_BEFORE"

echo "[rules] expire path (force expires_at in the past via SQL)"
docker compose exec -T postgres psql -U nmcc -d nmcc -c \
  "UPDATE personal_rules SET expires_at = NOW() - INTERVAL '1 hour' WHERE id = '${RULE_ID}';" >/dev/null

SYNC_EXPIRED="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" "${BASE_URL}/api/rules/sync")"
node -e '
const j = JSON.parse(process.argv[1]);
const id = process.argv[2];
if (!Array.isArray(j.rules)) { console.error(j); process.exit(2); }
if (j.rules.some((r) => r.id === id)) {
  console.error("expired rule must not appear in sync", j);
  process.exit(2);
}
console.log("[rules] expired rule omitted from sync OK");
' "$SYNC_EXPIRED" "$RULE_ID"

echo "[rules] renew again so rule is active for propose"
curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/rules/${RULE_ID}/renew" \
  -d '{"ttlPreset":"7d"}' >/dev/null

echo "[rules] propose personal rule to global list"
PROPOSE_JSON="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -H 'content-type: application/json' \
  -X POST "${BASE_URL}/api/proposals" \
  -d "{\"personalRuleId\":\"${RULE_ID}\"}")"
PROPOSAL_ID="$(node -e '
const j = JSON.parse(process.argv[1]);
if (!j.proposal?.id || j.proposal.status !== "pending") { console.error(j); process.exit(2); }
process.stdout.write(j.proposal.id);
' "$PROPOSE_JSON")"
echo "[rules] proposal id=${PROPOSAL_ID}"

echo "[rules] member cannot approve"
HTTP_MEMBER_APPROVE="$(curl -sS -o /tmp/nmcc-rules-member-approve.json -w '%{http_code}' \
  -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -X POST "${BASE_URL}/api/proposals/${PROPOSAL_ID}/approve" || true)"
if [[ "$HTTP_MEMBER_APPROVE" != "403" ]]; then
  echo "[rules] expected 403 for member approve, got HTTP ${HTTP_MEMBER_APPROVE}"
  cat /tmp/nmcc-rules-member-approve.json || true
  exit 1
fi
echo "[rules] member approve rejected (HTTP 403)"

echo "[rules] shared list must not contain pattern before approve"
SYNC_BEFORE="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" "${BASE_URL}/api/lists/sync?since=0")"
node -e '
const j = JSON.parse(process.argv[1]);
const pattern = process.argv[2];
if ((j.entries || []).some((e) => e.pattern === pattern && e.active)) {
  console.error("pattern leaked before approve", j);
  process.exit(2);
}
console.log("[rules] shared list clean before approve");
' "$SYNC_BEFORE" "$PATTERN"

echo "[rules] admin approve"
APPROVE_JSON="$(curl -fsS -c "$COOKIE_ADMIN" -b "$COOKIE_ADMIN" \
  -X POST "${BASE_URL}/api/proposals/${PROPOSAL_ID}/approve")"
node -e '
const j = JSON.parse(process.argv[1]);
if (j.proposal?.status !== "approved" || !Number.isFinite(j.listRevision)) {
  console.error(j); process.exit(2);
}
console.log("[rules] approve OK revision=" + j.listRevision);
' "$APPROVE_JSON"

echo "[rules] shared list contains approved pattern"
SYNC_AFTER="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" "${BASE_URL}/api/lists/sync?since=0")"
node -e '
const j = JSON.parse(process.argv[1]);
const pattern = process.argv[2];
const hit = (j.entries || []).find((e) => e.pattern === pattern && e.active === true);
if (!hit) {
  console.error("expected active pattern in shared list", pattern, j);
  process.exit(2);
}
console.log("[rules] shared list has pattern=" + hit.pattern + " source=" + hit.source);
' "$SYNC_AFTER" "$PATTERN"

echo "[rules] make-permanent + delete smoke"
PERM_JSON="$(curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -X POST "${BASE_URL}/api/rules/${RULE_ID}/make-permanent")"
node -e '
const j = JSON.parse(process.argv[1]);
if (j.rule?.ttlPreset !== "permanent" || j.rule.expiresAt !== null || j.rule.active !== true) {
  console.error(j); process.exit(2);
}
console.log("[rules] make-permanent OK");
' "$PERM_JSON"

curl -fsS -c "$COOKIE_USER" -b "$COOKIE_USER" \
  -X DELETE "${BASE_URL}/api/rules/${RULE_ID}" | grep -q '"ok":true'

echo "[rules] done"
