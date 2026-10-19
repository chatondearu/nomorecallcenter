# PLAN-2 — Local dev stack and device acceptance

- **Status**: in-progress
- **PRD**: [[../prd/PRD-1-mvp-and-vision|PRD-1]] (manual acceptance before `shipped`)
- **GitHub**: _(no dedicated issue — closes gap in [[PLAN-1-mvp-and-vision#Verification|PLAN-1 verification]])_
- **Date**: 2026-10-19

## Summary

Bring a developer machine from a fresh `main` checkout to a repeatable loop: API + Postgres via compose, admin UI on the host, Android client on emulator or physical device (USB or wireless ADB). Tick the remaining PRD manual path: login against a self-hosted instance, Call Screening role, synced lists/rules, silent reject on a seeded prefix.

## Constraints (from ADRs)

| ADR | Constraint |
| --- | ---------- |
| [[../foam/decisions/ADR-004-backend-stack|ADR-004]] | API in `@nmcc/app`; admin separate process; Postgres + Drizzle |
| [[../foam/decisions/ADR-005-auth-model|ADR-005]] | Better Auth; first user admin; invite-only after bootstrap |
| [[../foam/decisions/ADR-008-distribution-channels|ADR-008]] | Call Screening role required before “protected”; Android sideload / debug release |
| [[../foam/decisions/ADR-003-android-client-stack|ADR-003]] | minSdk 29; offline bundled list works without API |

## Scope

### In scope

- Sync repo + toolchain (Nix/direnv or documented prerequisites).
- One documented “happy path” for web (smoke + admin).
- Two Android paths: **offline-only** (no API) and **full stack** (LAN URL + auth).
- Physical device: same Wi‑Fi as host, `BETTER_AUTH_URL` aligned with Instance URL.
- Optional: `installDebug` via ADB (USB or `adb connect` after pairing).

### Out of scope

- Automating instrumented Call Screening on CI (matcher JVM tests already ship).
- HTTPS / reverse proxy for LAN (cleartext is intentional for local self-host).
- F-Droid or signed release channel setup.

## Technical approach

Tracer bullets — finish each slice before the next.

1. **Host toolchain** — `direnv allow` or `nix develop -c …`; `pnpm install`; Docker running.
2. **API slice** — `pnpm smoke` (compose, health, migrate); `lists:seed`; `auth:acceptance` or manual bootstrap from README.
3. **Admin slice** — `pnpm dev:admin`; login as bootstrap admin; spot-check proposals/stats pages.
4. **Android offline slice** — debug APK (`assembleDebug` or GitHub `v0.1.1-debug`); grant Call Screening; verify bundled `0892*` / `33892` reject without login.
5. **Android + API slice** — set Instance URL (`10.0.2.2:3000` emulator or `http://<host-lan-ip>:3000`); member sign-in; sync; personal TTL rule round-trip.
6. **Device acceptance** — with role held, incoming call matching seeded prefix silent-rejects; note OEM if behavior differs.

**LAN auth:** Compose `api` must expose port 3000 on `0.0.0.0`. Set `BETTER_AUTH_URL` (and recreate/restart `api` if already up) to the same base URL the phone uses, e.g. `http://192.168.x.x:3000`.

## Tasks

- [x] **Sync sources** — `git fetch` + rebase/merge `origin/main`; `pnpm install` (lockfile current).
- [x] **Toolchain smoke** — `POSTGRES_PORT=5437 pnpm smoke` (host 5432 busy); health OK @ `3000`.
- [x] **Seed + auth** — `auth:acceptance` on 5437; `lists:seed` after wipe (88 prefixes).
- [x] **Admin dev** — `pnpm dev:admin` → http://localhost:3001/login (HTTP 200).
- [x] **Android build** — `:matcher:test` + `assembleDebug` OK (`app-debug.apk`).
- [ ] **Android offline** — install debug APK; role granted; test prefix block without network/login.
- [ ] **Android + API (emulator or device)** — Instance URL + invited user; sync lists; create personal rule; see it after sync.
- [x] **LAN env** — `.env`: `POSTGRES_PORT=5437`, `BETTER_AUTH_URL=http://192.168.1.31:3000`; API recreated.
- [ ] **ADB install (optional)** — no device attached (`adb devices` empty).
- [ ] **Acceptance sign-off** — record device model + Android version + result in PRD / foam or close PLAN-1 verification checkbox.

## Risks & mitigations

| Risk | Mitigation |
| ---- | ---------- |
| Phone cannot reach API | Same Wi‑Fi; firewall allows 3000; use LAN IP not `localhost` |
| Login fails from phone only | Match `BETTER_AUTH_URL` to Instance URL; restart API container |
| OEM blocks Call Screening | Test stock-ish device first; document OEM in acceptance notes |
| Debug APK won’t install over old build | Bump `versionCode` (already 2 on `main`) or uninstall previous debug app |

## Test plan

- [x] `pnpm smoke` _(with `POSTGRES_PORT=5437`)_
- [x] `pnpm auth:acceptance` _(same)_
- [ ] `pnpm admin:acceptance` _(optional; wipes volumes)_
- [x] `cd android && ./gradlew :matcher:test :app:assembleDebug`
- [ ] Manual: Call Screening + seeded prefix on physical device or emulator

## Verification

- [x] Contributor docs: [doc/local-development.md](../doc/local-development.md) (+ README / `.env.example` cross-links)
- [x] Web design loop: [design/README.md](../design/README.md), `pnpm dev:design`, Playwright captures
- [x] Android emulator eyes — `NMCC_DESIGN_ROUTE` intent, `android:design:capture`, [android-emulator.md](../design/android-emulator.md)
- [ ] PLAN-1 manual path checkbox marked done with evidence (date, device, commit `cc63940` or later)
- [ ] No new ADR unless LAN auth needs a product change (env-only is enough for dev)
