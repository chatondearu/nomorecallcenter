# Deployment

Self-host path for operators (ADR-008): **OCI + docker compose** for the API;
admin is a separate Nuxt app run next to the API (not a second compose service
in MVP). Android: GitHub Releases / F-Droid — see [`android/README.md`](../android/README.md).

## Full self-host smoke

End-to-end from empty machine to a screening-capable client:

```bash
# 1. Clone and install (host tools for migrate/seed)
pnpm install

# 2. Postgres + API (local OCI build tagged nmcc-api by default)
docker compose up -d --build
curl -fsS http://127.0.0.1:3000/api/health

# 3. Schema + default FR list seed (ARCEP LO 2.0 → CC BY-SA; see NOTICE)
DATABASE_URL=postgresql://nmcc:nmcc@localhost:5432/nmcc pnpm db:migrate
DATABASE_URL=postgresql://nmcc:nmcc@localhost:5432/nmcc pnpm lists:seed

# 4. Bootstrap admin (first sign-up on empty instance)
curl -fsS -c /tmp/nmcc-admin.txt -X POST http://127.0.0.1:3000/api/auth/sign-up/email \
  -H 'content-type: application/json' \
  -d '{"name":"Admin","email":"admin@example.com","password":"change-me-now"}'

# 5. Invite a member
curl -fsS -b /tmp/nmcc-admin.txt -X POST http://127.0.0.1:3000/api/invites \
  -H 'content-type: application/json' \
  -d '{}'
# → use inviteToken on the second user’s sign-up

# 6. Admin UI (moderation / stats) — separate process
pnpm dev:admin
# http://127.0.0.1:3001 — login as bootstrap admin

# 7. Android: install APK, set Instance URL → API base, sign in, grant Call Screening
# Emulator → host API: http://10.0.2.2:3000
# Physical device → LAN IP of the host, e.g. http://192.168.1.10:3000
```

Bundled scripts (wipe volumes where noted):

```bash
pnpm smoke              # compose up → health → migrate
pnpm auth:acceptance    # admin → invite → second user → reject open signup
pnpm lists:acceptance   # ARCEP seed → GET /api/lists/sync
pnpm admin:acceptance   # propose → approve via admin UI path
```

Stop: `pnpm compose:down` (add `-v` to wipe Postgres). Change `POSTGRES_PASSWORD`
and `BETTER_AUTH_SECRET` before any real deployment (see `.env.example`).

## OCI images (API)

Dockerfile: `modules/app/Dockerfile` (build context = monorepo root).

Compose service `api` uses:

| Variable | Default | Role |
| -------- | ------- | ---- |
| `DOCKER_IMAGE_API` | `nmcc-api` | Image name/tag for the API container |

### Local build (default)

```bash
docker compose build api
# or
docker build -f modules/app/Dockerfile -t nmcc-api .
docker compose up -d
```

### Pull a pre-built image (GHCR)

CI workflow [`.github/workflows/oci-api.yml`](../.github/workflows/oci-api.yml)
pushes `ghcr.io/<owner>/nmcc-api` on `main`, tags `v*` / `api-v*`, and
`workflow_dispatch`.

```bash
# Example after packages are public (or you are logged in to GHCR)
export DOCKER_IMAGE_API=ghcr.io/<owner>/nmcc-api:main
docker pull "$DOCKER_IMAGE_API"
docker compose up -d   # do not pass --build if you want the pulled tag
```

Private packages: `echo $GITHUB_TOKEN | docker login ghcr.io -u USER --password-stdin`.

### Admin (not an OCI service in MVP compose)

`@nmcc/admin` stays a host/dev process for MVP:

```bash
pnpm --filter @nmcc/admin build
pnpm --filter @nmcc/admin preview   # or pnpm dev:admin
```

Set `ADMIN_URL` on the API when the admin origin is not `http://localhost:3001`.
Proxy: `API_PROXY_TARGET` (default `http://127.0.0.1:3000`). Shipping a second
admin image is deferred (PRD open question).

## Compose details

```bash
pnpm install
docker compose up -d --build
curl -fsS http://127.0.0.1:3000/api/health
DATABASE_URL=postgresql://nmcc:nmcc@localhost:5432/nmcc pnpm db:migrate
DATABASE_URL=postgresql://nmcc:nmcc@localhost:5432/nmcc pnpm lists:seed
```

`pnpm lists:seed` fetches ARCEP MAJNUM (Licence Ouverte 2.0), writes `0892*`
demarchage prefixes into `list_entries`, and bumps `schema_meta.lists.revision`.
Use `ARCEP_SEED_FIXTURE=1` or `--fixture` offline. Attribution text lives in
[`NOTICE`](../NOTICE) and per-row `attribution` / `source_date`.

Services: Postgres 17 + API image from `modules/app/Dockerfile`. Ports and env
defaults match `.env.example`. Busy ports: `API_PORT=3017 POSTGRES_PORT=5437 …`.

## Admin UI

Local (API already up on port 3000):

```bash
pnpm dev:admin
# http://127.0.0.1:3001 — login as bootstrap admin → Propositions → Approuver
```

Acceptance (alternate ports; wipes compose volumes): `pnpm admin:acceptance`.

## Android client

See [`android/README.md`](../android/README.md) (debug/release APK/AAB, F-Droid notes).

1. Build APK (JDK 17 + Android SDK 35).
2. Install; set **Instance URL** to your API.
3. Sign in; sync; grant **Call Screening** — UI does not claim “protected” until the role is held (ADR-008).
