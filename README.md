# nomorecallcenter

Open-source Android call-screening client with a self-hosted multi-user backend
(Nuxt/Nitro + Postgres + Drizzle). AGPL-3.0.

Status: Slice 3 personal TTL rules + moderated propose/approve APIs.

Product docs: [`prd/PRD-1-mvp-and-vision.md`](prd/PRD-1-mvp-and-vision.md) ·
[`plans/PLAN-1-mvp-and-vision.md`](plans/PLAN-1-mvp-and-vision.md) ·
[`DESIGN.md`](DESIGN.md)

Full documentation: [`doc/README.md`](doc/README.md)

## Monorepo layout

| Path | Package | Role |
| ---- | ------- | ---- |
| `modules/db` | `@nmcc/db` | Drizzle schema, client, migrate |
| `modules/shared` | `@nmcc/shared` | Nuxt layer — UnoCSS + MD3 tokens (`#1A6352`) |
| `modules/app` | `@nmcc/app` | Public API (Nitro REST) |
| `modules/admin` | `@nmcc/admin` | Admin UI skeleton |
| `android/` | — | Kotlin client placeholder (ADR-003) |

## Prerequisites

- Node.js ≥ 22, pnpm ≥ 10
- Docker + Docker Compose

## Self-host smoke path

```bash
# 1. Install workspace deps (once, for migrate from the host)
pnpm install

# 2. Bring up Postgres + API
docker compose up -d --build

# 3. Health check (expect HTTP 200 + {"ok":true,...})
curl -fsS http://127.0.0.1:3000/api/health

# 4. Apply Drizzle migrations on the fresh DB
DATABASE_URL=postgresql://nmcc:nmcc@localhost:5432/nmcc pnpm db:migrate
```

Or run the bundled scripts:

```bash
pnpm install
pnpm smoke              # compose up → health → migrate
pnpm auth:acceptance    # wipe volumes → admin → invite → second user → reject open signup
pnpm lists:acceptance   # wipe → migrate → admin → ARCEP seed → GET /api/lists/sync
pnpm rules:acceptance   # wipe → 7d rule → renew → propose → admin approve → shared list
```

If ports `5432`/`3000` are busy, override: `API_PORT=3001 POSTGRES_PORT=5433 pnpm rules:acceptance`.

### Auth bootstrap (Slice 1)

1. First successful `POST /api/auth/sign-up/email` on an empty instance becomes **admin**.
2. Optional harden: set `BOOTSTRAP_ADMIN_EMAIL` so only that address may register first.
3. After bootstrap, signup requires a valid `inviteToken` (admin: `POST /api/invites`).
4. Session check: `GET /api/me` (cookie session from Better Auth).

### Shared lists (Slice 2)

1. After migrate: `DATABASE_URL=… pnpm lists:seed` fetches ARCEP MAJNUM (LO 2.0) and upserts `0892*` demarchage prefixes (ADR-002 — no third-party dumps in the repo).
2. Clients pull with a session cookie: `GET /api/lists/sync?since=0` (full active snapshot) or `since=<revision>` (deltas + tombstones).
3. Offline: `pnpm lists:seed -- --fixture` or `ARCEP_SEED_FIXTURE=1`.

### Personal rules + proposals (Slice 3)

1. Member creates a personal rule: `POST /api/rules` with `{ pattern, matchType, ttlPreset }` (`1h`/`24h`/`7d`/`30d`/`permanent`).
2. Sync active rules: `GET /api/rules/sync` (expired omitted). Lifecycle: `…/renew`, `…/make-permanent`, `DELETE`.
3. Propose to global: `POST /api/proposals` with `{ personalRuleId }` — stays `pending` (no shared-list mutate).
4. Admin: `POST /api/proposals/:id/approve` upserts shared list; `reject` closes. Members get `403` on moderate.

Stop the stack: `pnpm compose:down` (add `-v` to wipe the Postgres volume).

Default credentials match [`.env.example`](.env.example) (`nmcc` / `nmcc`). Change Postgres password and `BETTER_AUTH_SECRET` before any real deployment.

## Local web (optional)

```bash
pnpm install
pnpm dev:api     # http://localhost:3000
pnpm dev:admin   # http://localhost:3001
```

## License

Copyright (C) 2026 nomorecallcenter contributors.

This program is free software under the [GNU Affero General Public License v3.0](LICENSE)
(AGPL-3.0-only). Default list data is **derived** from ARCEP / data.gouv MAJNUM under
**Licence Ouverte 2.0** (attribution + date on each entry); our published list policy is
CC BY-SA (ADR-001 / ADR-002).
