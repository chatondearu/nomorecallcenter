# nomorecallcenter

Open-source Android call-screening client with a self-hosted multi-user backend
(Nuxt/Nitro + Postgres + Drizzle). AGPL-3.0.

Status: Slice 0 scaffold (compose + API health + Drizzle migrate + MD3/UnoCSS tokens).

Product docs: [`prd/PRD-1-mvp-and-vision.md`](prd/PRD-1-mvp-and-vision.md) ·
[`plans/PLAN-1-mvp-and-vision.md`](plans/PLAN-1-mvp-and-vision.md) ·
[`DESIGN.md`](DESIGN.md)

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

Or run the bundled script:

```bash
pnpm install
pnpm smoke
```

Stop the stack: `pnpm compose:down` (add `-v` to wipe the Postgres volume).

Default credentials match [`.env.example`](.env.example) (`nmcc` / `nmcc`). Change them before any real deployment.

## Local web (optional)

```bash
pnpm install
pnpm dev:api     # http://localhost:3000
pnpm dev:admin   # http://localhost:3001
```

## License

Copyright (C) 2026 nomorecallcenter contributors.

This program is free software under the [GNU Affero General Public License v3.0](LICENSE)
(AGPL-3.0-only). Default list data licenses are documented separately when shipped.
