# Local development

Day-to-day workflow for contributors: API in Docker, admin on the host, optional
Android APK. Operators following a production-like install should still read
[deployment.md](deployment.md).

## Prerequisites

| Tool | Role |
| ---- | ---- |
| **Docker + Compose** | Postgres + API (`nmcc-api` image built from `modules/app/Dockerfile`) |
| **Node 22 + pnpm 10** | Migrations, seeds, `pnpm dev:admin`, acceptance scripts |
| **JDK 17 + Android SDK 35** | Only for `android/` builds (optional for API-only work) |

**Recommended:** [Nix + direnv](../README.md#dev-shell-nix--direnv) at the repo root
(`direnv allow`, then `nix develop -c …` for any command). Without Nix, install the
same versions yourself and set `JAVA_HOME` / `ANDROID_HOME` per
[android/README.md](../android/README.md).

## First-time setup

```bash
git clone …/nomorecallcenter.git && cd nomorecallcenter
direnv allow                    # if you use direnv
nix develop -c pnpm install     # or: pnpm install

cp .env.example .env            # gitignored; Compose reads it automatically
```

Edit `.env` when defaults conflict with your machine (see [Port conflicts](#port-conflicts)
and [Android on a physical device](#android-on-a-physical-device)).

## Port conflicts

Compose maps Postgres and the API to the host:

| Variable | Default | Used for |
| -------- | ------- | -------- |
| `POSTGRES_PORT` | `5432` | Host → Postgres |
| `API_PORT` | `3000` | Host → API (`GET /api/health`) |

If `5432` or `3000` is already taken, set alternate values in `.env` (or export them
for one command). Scripts (`scripts/smoke.sh`, `scripts/auth-acceptance.sh`, etc.)
read `POSTGRES_PORT` and build `DATABASE_URL` as
`postgresql://nmcc:nmcc@localhost:${POSTGRES_PORT}/nmcc` unless `DATABASE_URL` is set.

Example when another Postgres uses 5432:

```bash
# .env
POSTGRES_PORT=5437
DATABASE_URL=postgresql://nmcc:nmcc@localhost:5437/nmcc
API_PORT=3000
```

Then run compose and scripts **without** repeating the port on every line — Compose
loads `.env` from the project root.

## Bring the API up (fast path)

```bash
pnpm smoke
# compose up --build → wait for health → drizzle migrate
```

Seed the default FR demarchage list (needs network unless you use a fixture):

```bash
pnpm lists:seed
# or: ARCEP_SEED_FIXTURE=1 pnpm lists:seed
```

Verify:

```bash
curl -fsS http://127.0.0.1:${API_PORT:-3000}/api/health
```

### Clean auth bootstrap (optional)

Wipes compose volumes, recreates the stack, migrates, and runs the invite flow:

```bash
pnpm auth:acceptance
```

Default test accounts (override with `AUTH_ACCEPT_*` env vars — see
`scripts/auth-acceptance.sh`):

| Role | Email | Password |
| ---- | ----- | -------- |
| Admin | `admin@example.com` | `AdminPass123!` |
| Member | `user@example.com` | `UserPass123!` |

After `auth:acceptance`, run `pnpm lists:seed` again if you need list data (the script
does not seed lists).

## Admin UI

The API runs in Docker; the admin app is a **separate dev process** (MVP, ADR-008).

```bash
pnpm dev:admin
# http://localhost:3001 — proxies /api/** to API_PROXY_TARGET (default http://127.0.0.1:3000)
```

Log in with the bootstrap admin from acceptance or manual sign-up. Nuxt may bind
`localhost` on IPv6 only; use `http://localhost:3001`, not only `127.0.0.1`, if one
fails.

### Public site + API in Node (optional)

For landing/UI work without rebuilding the Docker image:

```bash
# Postgres must be reachable (compose up with matching DATABASE_URL in .env)
pnpm dev:api
# http://localhost:3000 — same port as compose; do not run both on 3000
```

Use **either** compose API **or** `pnpm dev:api` on port 3000, not both.

## Android

### Build

```bash
nix develop -c bash -c 'cd android && ./gradlew :matcher:test :app:assembleDebug'
# APK: android/app/build/outputs/apk/debug/app-debug.apk
```

### Install on a device

USB debugging (or wireless ADB after pairing):

```bash
adb devices
nix develop -c bash -c 'cd android && ./gradlew :app:installDebug'
```

Pre-built debug APK: [GitHub Releases](https://github.com/chatondearu/nomorecallcenter/releases)
(tag `v*-debug`).

### Offline filtering (no backend)

Install the APK → grant **Call Screening** → test bundled prefixes (`0892*`, `33892`)
or personal rules. No login required. See [android/README.md](../android/README.md).

### Android on a physical device (sync + login)

1. Phone and PC on the **same Wi‑Fi**.
2. **Instance URL** in the app: `http://<your-pc-lan-ip>:3000` (not `localhost`).
3. Set **`BETTER_AUTH_URL`** in `.env` to that **same** base URL, then recreate the API
   container so Better Auth matches what the client calls:

   ```bash
   # .env example — replace with your LAN IP
   BETTER_AUTH_URL=http://192.168.1.31:3000
   docker compose up -d --force-recreate api
   ```

4. Sign in with an **invited** member account; grant Call Screening; sync lists/rules.

**Emulator:** use `http://10.0.2.2:3000` as the instance URL; keep
`BETTER_AUTH_URL=http://10.0.2.2:3000` in `.env` when testing auth from the emulator.

Cleartext HTTP is allowed in the debug app for local self-host
(`network_security_config.xml`).

## Acceptance scripts

| Script | What it does |
| ------ | ------------- |
| `pnpm smoke` | Compose up, health, migrate |
| `pnpm auth:acceptance` | Wipe volumes → bootstrap admin → invite → second user |
| `pnpm lists:acceptance` | Seed + `GET /api/lists/sync` |
| `pnpm rules:acceptance` | Personal rules API |
| `pnpm stats:acceptance` | Stats aggregates |
| `pnpm admin:acceptance` | Moderation path (alternate ports; may wipe volumes) |

All respect `API_PORT`, `POSTGRES_PORT`, and `DATABASE_URL` like `smoke`.

## Stop and reset

```bash
pnpm compose:down        # keep Postgres data
pnpm compose:down -v     # wipe DB volume — next up is empty instance
```

Stop `pnpm dev:admin` / `pnpm dev:api` with Ctrl+C in their terminals.

## Design loop (`cda-design-loop`)

Visual review for the **public site** and **admin** (not the Android APK). Brief:
[`DESIGN.md`](../DESIGN.md). Route list: [`design/routes.json`](../design/routes.json).

```bash
pnpm install
# Nix shell sets PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH (no download). Else: npx playwright install chromium

pnpm dev:design                    # Postgres + Nuxt @nmcc/app + @nmcc/admin (stops compose API if it was on :3000)
# In another terminal, after a fresh DB:
pnpm auth:acceptance               # optional once — creates admin@example.com / AdminPass123!
pnpm design:auth                   # writes .playwright/admin.json
pnpm design:capture                # design/shots/{site,admin}/…/w375.png …
```

Environment (optional in `.env`): `DEV_URL_SITE`, `DEV_URL_ADMIN`, `DESIGN_ADMIN_EMAIL`,
`DESIGN_ADMIN_PASSWORD`. Single-route capture:

```bash
pnpm design:shot http://127.0.0.1:3000/ /tmp/nmcc-site-shot
pnpm design:shot http://127.0.0.1:3001/proposals /tmp/nmcc-admin-shot .playwright/admin.json
```

See [`design/README.md`](../design/README.md).

### Android UI captures (emulator)

```bash
pnpm android:emulator:daemon      # once per session
pnpm android:design:capture       # design/shots-android/*.png
```

Details: [`design/android-emulator.md`](../design/android-emulator.md). Emulator API uses `http://10.0.2.2:3000` for the host API.

## Checklist (device acceptance)

Tracked in [plans/PLAN-2-local-dev-and-device-acceptance.md](../plans/PLAN-2-local-dev-and-device-acceptance.md):
compose + admin + APK on a real device or emulator, silent reject on a seeded prefix
with Call Screening granted.
