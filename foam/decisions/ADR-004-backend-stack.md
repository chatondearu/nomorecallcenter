# ADR-004 — Self-host backend stack (BAJO-shaped)

- **Status**: accepted
- **Date**: 2026-10-13
- **Tags**: #backend #nuxt #drizzle #mvp #explore

## Context

MVP needs a multi-user self-hostable backend (lists, proposals, moderation, analytics) plus a web admin. The operator already runs [BAJO](https://github.com/BarAJeuxOnline/barajeuxonline) with a proven monorepo pattern; reusing that shape beats introducing Go/Rust.

Android client remains native Kotlin (ADR-003).

## Decision

1. **Monorepo TypeScript** inspired by BAJO: `pnpm` workspaces under `modules/*`.
2. **Nuxt + Nitro** for the HTTP API (`server/api` REST JSON) and **Nuxt admin** (moderation / ops UI).
3. **PostgreSQL + Drizzle** in a dedicated `modules/db` (schema, migrations, client) — not SQLite for MVP.
4. **REST JSON** at MVP; **no GraphQL** unless a later ticket reopens it for proven mobile over/under-fetch pain.
5. **Docker image(s) + Compose** (app/API, admin if separate, Postgres) in the BAJO style.
6. **Android** stays Kotlin in-repo (`android/` or `modules/android`) but outside the Node runtime.

Minimal modules day one: `db`, Nuxt API/app, `admin`, `shared`, plus Android tree.

## Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Go single binary | Clean ops, but duplicates team’s BAJO skills and slows admin UI |
| SQLite default | Operator preference + BAJO parity; Postgres from day one |
| GraphQL at MVP | Extra stack vs BAJO Nitro REST; features are CRUD/sync-shaped |
| Flutter/shared UI with backend | Contradicts ADR-003 |

## Consequences

- **Positive**: Fastest path for this team; shared patterns (Drizzle, Nuxt, Docker) with BAJO.
- **Negative**: Heavier self-host footprint than a Go+SQLite box (Node + Postgres).
- **Constraint**: Auth details deferred to [Decide: Auth model…](https://github.com/chatondearu/nomorecallcenter/issues/7) (BAJO uses better-auth — candidate, not locked here).

## References

- [Decide: Self-host backend stack](https://github.com/chatondearu/nomorecallcenter/issues/6)
- BAJO: https://github.com/BarAJeuxOnline/barajeuxonline
- [[ADR-001-mvp-product-constraints|ADR-001]], [[ADR-003-android-client-stack|ADR-003]]
