# nomorecallcenter

Open-source Android call-screening client with a self-hosted multi-user backend
(Nuxt/Nitro + Postgres + Drizzle). AGPL-3.0.

Status: MVP packaging — OCI/compose, GitHub/F-Droid notes, self-host docs (Slice 7).

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
| `modules/admin` | `@nmcc/admin` | Admin UI (moderation, stats, invites) |
| `android/` | — | Kotlin Compose Call Screening client (ADR-003) |

## Prerequisites

- Node.js ≥ 22, pnpm ≥ 10
- Docker + Docker Compose
- Android: JDK 17 + SDK 35 for APK builds

## Dev shell (Nix + direnv)

Preferred local setup for humans and agents — pins Node 22, pnpm, JDK 17,
Android SDK (platforms 35 + build-tools 34/35), and docker-compose.

```bash
# First time (or after cloning)
direnv allow          # interactive terminals with direnv
# Agents / CI without direnv: wrap every command
nix develop -c pnpm install
nix develop -c bash -c 'cd android && ./gradlew :matcher:test :app:assembleDebug'
```

If `flake.nix` / `.envrc` are new and untracked, `git add` them first so Nix
can see the flake. Debug APK:
`android/app/build/outputs/apk/debug/app-debug.apk`.

Without Nix: install the prerequisites above yourself and keep
`JAVA_HOME` / `ANDROID_HOME` set (see [`android/README.md`](android/README.md)).

## Self-host smoke path

Compose → bootstrap → invite → Android URL → Call Screening role:

```bash
pnpm install
docker compose up -d --build
curl -fsS http://127.0.0.1:3000/api/health

DATABASE_URL=postgresql://nmcc:nmcc@localhost:5432/nmcc pnpm db:migrate
DATABASE_URL=postgresql://nmcc:nmcc@localhost:5432/nmcc pnpm lists:seed

# First sign-up on an empty instance becomes admin
curl -fsS -c /tmp/nmcc-admin.txt -X POST http://127.0.0.1:3000/api/auth/sign-up/email \
  -H 'content-type: application/json' \
  -d '{"name":"Admin","email":"admin@example.com","password":"change-me-now"}'

# Invite a member (copy inviteToken into the second sign-up)
curl -fsS -b /tmp/nmcc-admin.txt -X POST http://127.0.0.1:3000/api/invites \
  -H 'content-type: application/json' \
  -d '{}'

pnpm dev:admin   # http://127.0.0.1:3001 — moderation UI
```

Then on Android (see [`android/README.md`](android/README.md)):

1. Install a debug/release APK.
2. Set **Instance URL** to the API (`http://10.0.2.2:3000` on emulator, or your LAN IP).
3. Sign in with the invited account.
4. Grant **Call Screening** — the app does not claim “protected” until the role is held.
5. Sync; a seeded `0892*` prefix should silent-reject when the role is held.

Or run bundled scripts:

```bash
pnpm smoke              # compose up → health → migrate
pnpm auth:acceptance    # wipe → admin → invite → second user
pnpm lists:acceptance   # ARCEP seed → GET /api/lists/sync
pnpm rules:acceptance
pnpm stats:acceptance
pnpm admin:acceptance
```

Busy ports: `API_PORT=3017 POSTGRES_PORT=5437 pnpm …`. Stop: `pnpm compose:down` (`-v` wipes DB).

Pre-built API image (GHCR): set `DOCKER_IMAGE_API=ghcr.io/<owner>/nmcc-api:<tag>` and
`docker compose up -d` without `--build`. Details: [`doc/deployment.md`](doc/deployment.md).

Default credentials match [`.env.example`](.env.example). Change Postgres password and
`BETTER_AUTH_SECRET` before any real deployment.

### Feature notes

| Area | Pointer |
| ---- | ------- |
| Auth bootstrap / invites | First sign-up = admin; then invite-only (`POST /api/invites`) |
| Shared lists | `pnpm lists:seed` (ARCEP LO 2.0); `GET /api/lists/sync` |
| Personal rules / proposals | `POST /api/rules`, `POST /api/proposals`, admin approve/reject |
| OCI + admin process | [`doc/deployment.md`](doc/deployment.md) |

## Android client

```bash
cd android
./gradlew :matcher:test
./gradlew :app:assembleDebug          # needs ANDROID_HOME
./gradlew :app:assembleRelease :app:bundleRelease   # GitHub Release artifacts
```

F-Droid-oriented notes (no Play-only deps): [`android/README.md`](android/README.md).

## Local web (optional)

```bash
pnpm install
pnpm dev:api     # http://localhost:3000 — French landing and API
pnpm dev:admin   # http://localhost:3001
```

## License

Copyright (C) 2026 nomorecallcenter contributors.

This program is free software under the [GNU Affero General Public License v3.0](LICENSE)
(AGPL-3.0-only). Default list data is **derived** from ARCEP / data.gouv MAJNUM under
**Licence Ouverte 2.0**; our published list policy is **CC BY-SA**. Full attribution:
[`NOTICE`](NOTICE) (ADR-001 / ADR-002).
