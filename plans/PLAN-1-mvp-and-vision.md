# PLAN-1 — nomorecallcenter MVP + vision

- **Status**: draft
- **PRD**: [[../prd/PRD-1-mvp-and-vision|PRD-1-mvp-and-vision]]
- **GitHub**: [#1](https://github.com/chatondearu/nomorecallcenter/issues/1)
- **Date**: 2026-10-13

## Summary

Implement the approved MVP: Kotlin Call Screening Android client + BAJO-shaped Nuxt/Nitro/Postgres self-host stack (auth, FR default lists, moderated proposals, personal TTL rules sync, analytics) with GitHub/F-Droid and OCI/compose distribution. Vision items stay documentation-only in foam/PRD — not build gates.

## Constraints (from ADRs)

| ADR | Constraint |
| --- | ---------- |
| [[../foam/decisions/ADR-001-mvp-product-constraints\|ADR-001]] | Multi-user self-host; admin-moderated proposals; hash/prefix metadata; silent reject; FR lists first; AGPL + CC BY-SA lists |
| [[../foam/decisions/ADR-002-default-list-sources\|ADR-002]] | Seed from ARCEP/data.gouv (LO 2.0); do not vendor Saracroche/Begone/PhoneBlock dumps |
| [[../foam/decisions/ADR-003-android-client-stack\|ADR-003]] | Kotlin + Jetpack Compose; minSdk 29; native CallScreeningService |
| [[../foam/decisions/ADR-004-backend-stack\|ADR-004]] | pnpm monorepo; Nuxt/Nitro REST; Postgres + Drizzle; Docker/Compose; no GraphQL MVP |
| [[../foam/decisions/ADR-005-auth-model\|ADR-005]] | Better Auth + Drizzle; first-user admin; invite-only; email/password |
| [[../foam/decisions/ADR-006-mvp-stats-surface\|ADR-006]] | User/instance counters; admin funnel + admin-only near-live event feed |
| [[../foam/decisions/ADR-007-block-ttl-model\|ADR-007]] | Personal TTL presets; server source of truth; propose-to-global CTA |
| [[../foam/decisions/ADR-008-distribution-channels\|ADR-008]] | GitHub Releases + F-Droid; OCI + compose; screening role onboarding |

Do not violate accepted ADRs without superseding them.

## Scope

### In scope

- Monorepo scaffold (db, shared, API/app, admin, android) + compose
- Auth, lists, proposals, personal rules, call-event ingestion, stats APIs
- Android screening + sync + stats + onboarding
- Default FR list generation pipeline + license attribution
- Minimal docs for self-host and sideload/F-Droid build notes
- Vision note already in PRD/foam — no further explore tickets

### Out of scope

- Play Store, GraphQL, OIDC, federation, iOS, SMS, ML auto-scoring, open signup (see PRD non-goals)

## Technical approach

Tracer-bullet order: **slice 0** runnable compose + empty Nitro health; then **auth**; then **lists + sync**; then **personal rules + TTL**; then **call events + stats**; then **Android screening wired to the same APIs**; then **admin moderation + live feed**; then **packaging**. Prefer BAJO patterns (pnpm workspaces, Drizzle in `db`, Better Auth, Nitro `server/api`) without copying unrelated BAJO modules (bot, Discord, etc.).

**Seams (test at these boundaries):**

1. REST JSON API contract (auth, lists, rules, proposals, events, stats)
2. Drizzle schema + migrations (idempotent migrate on fresh Postgres)
3. Call-rule matcher (prefix/exact + TTL active?) — pure function / unit seam
4. Android screening decision given handle + rule set (unit / instrumented)
5. Compose bring-up (health + migrate + first admin bootstrap)

## Tasks

### Slice 0 — Skeleton

- [ ] **Design system foundation** (ADR-009): keep `DESIGN.md` + `design/tokens` as source of truth; shared layer must load MD3/UnoCSS tokens before real UI screens (Histoire can land with first components).
- [ ] **Monorepo bootstrap**: pnpm workspace with `db`, `shared`, Nuxt API (or app), `admin`, and an Android project root; root scripts for typecheck/lint placeholders; AGPL license file present.
- [ ] **Compose skeleton**: `docker compose` brings up Postgres + API container; `GET` health returns 200; volume persists Postgres data.
- [ ] **Drizzle module**: schema package exports client + migrate command; empty migration applies on fresh DB.

### Slice 1 — Auth

- [ ] **Better Auth wired**: email/password sign-up/sign-in against Postgres auth tables; session usable from Nitro route guards.
- [ ] **Bootstrap admin**: on empty user table, first successful registration becomes admin (honor optional bootstrap email env if set).
- [ ] **Invites**: admin can create invite; registration without valid invite fails after bootstrap; invite marks used.
- [ ] **Acceptance**: compose stack → register admin → create invite → second user joins → non-invite sign-up rejected.

### Slice 2 — Lists

- [ ] **List schema**: shared list entries (prefix pattern, source, attribution metadata, active flag).
- [ ] **ARCEP seed pipeline**: script/job fetches or loads open data, writes seed entries with LO 2.0 attribution + date; no third-party dump vendored in repo.
- [ ] **List sync API**: authenticated client can pull list revision/`since` and receive entries; inactive entries tombstoned or omitted per chosen sync contract.
- [ ] **Acceptance**: seed produces ≥1 known FR demarchage-style prefix; sync endpoint returns it to a member session.

### Slice 3 — Personal rules + propose

- [ ] **Personal rules API**: CRUD for exact/prefix rules with TTL presets (1h/24h/7d/30d/permanent); server stores `expiresAt` or null; renew / make-permanent / delete behaviors.
- [ ] **Sync**: member devices pull personal rules for the account; expired rules not returned as active.
- [ ] **Propose CTA backend**: from a rule or free-form payload, create proposal in `pending`; does not mutate shared list until admin approve.
- [ ] **Admin moderation**: approve applies to shared list; reject closes proposal; member cannot approve.
- [ ] **Acceptance**: create 7d rule → renew → expire path (time mocked or short TTL in test) → propose → admin approve → shared list contains pattern.

### Slice 4 — Events + stats

- [ ] **Event ingest**: POST events with prefix/hash, action, timestamp; reject clear full numbers if sent (or normalize to hash/prefix only); attach user id from session.
- [ ] **User stats**: counters screened/blocked/allowed for 7d/30d + top prefixes + recent personal history.
- [ ] **Instance stats**: aggregates across users for members; admin also gets proposal funnel counts + event volume.
- [ ] **Admin live feed**: admin-only endpoint (poll or SSE) returns recent instance events (prefix/hash, action, user id, time).
- [ ] **Retention**: configurable retention env/setting; older events purged or omitted by job/query.
- [ ] **Acceptance**: ingest N events as two users → personal vs instance aggregates differ correctly; non-admin cannot read live feed.

### Slice 5 — Android client

- [ ] **App shell**: Compose app, minSdk 29; settings for instance base URL; login with email/password against Better Auth/API.
- [ ] **Call Screening role onboarding**: request `ROLE_CALL_SCREENING`; UI does not claim protected until role held.
- [ ] **Screening service**: `onScreenCall` matches shared list + active personal rules; default silent reject; responds within platform budget.
- [ ] **Rule matcher**: shared pure matching logic covered by unit tests (prefix/exact/TTL).
- [ ] **Sync loop**: pull lists + personal rules; push local-originated personal rule changes; propose-to-global action calls API.
- [ ] **Stats screens**: personal counters/history; optional instance aggregates view for members.
- [ ] **Event reporting**: after screen decision, enqueue ingest (best-effort; offline queue acceptable).
- [ ] **Acceptance**: instrumented or device test — synthetic handle matching a seeded prefix is rejected when role held; personal TTL rule syncs after create on API.

### Slice 6 — Admin UI

- [ ] **Admin auth**: staff/admin session required for moderation pages.
- [ ] **Proposals queue**: list pending; approve/reject actions.
- [ ] **Stats + live feed**: show funnel + near-live events for admin.
- [ ] **Acceptance**: admin completes propose→approve without using raw SQL.

### Slice 7 — Packaging & docs

- [ ] **OCI images**: build/publish path for API (and admin if separate) to GHCR or local tags documented in compose.
- [ ] **Android release notes**: GitHub Release APK/AAB build instructions; F-Droid-oriented notes (no Play-only deps).
- [ ] **README self-host**: compose up, bootstrap, invite, point Android at URL, grant screening role.
- [ ] **License headers / NOTICE**: AGPL for code; CC BY-SA + ARCEP attribution for shipped list data.
- [ ] **Vision**: ensure foam vision + PRD Vision section remain the only vision deliverable (no code for deferred themes).

## Risks & mitigations

| Risk | Mitigation |
| ---- | ---------- |
| OEM Call Screening quirks | Document known limits; test on ≥1 stock-ish + 1 OEM device before calling MVP done |
| Better Auth mobile session shape | Spike early in Slice 1/5; lock token/cookie approach in a short ADR if it diverges from BAJO web |
| ARCEP data shape drift | Pin source URL + date in seed metadata; pipeline must be re-runnable |
| Scope creep into Play/ML/federation | Refuse in review; point at PRD non-goals |
| Huge monorepo copy from BAJO | Copy patterns, not Discord/bot/catalog modules |

## Test plan

Seams under test (see Technical approach):

- [ ] **API auth/invite**: integration tests against Postgres (testcontainer or compose profile).
- [ ] **List seed + sync**: unit/integration on seed output + sync contract.
- [ ] **Rule matcher + TTL**: pure unit tests (no Android runtime).
- [ ] **Proposal moderation**: integration — pending → approve/reject side effects.
- [ ] **Stats aggregates**: integration with seeded events for two users.
- [ ] **Android matcher / screening decision**: JVM unit tests for matcher; optional instrumented test for service wiring.
- [ ] **Compose smoke**: scripted up → health → migrate → bootstrap admin.

## Verification

- [ ] Lint / typecheck / test / build for Node modules and Android debug assemble
- [ ] Manual path: compose → invite → Android login → role grant → prefix block observed
- [ ] Update foam / ADR if a new technical choice appears (e.g. mobile auth token shape)
- [ ] PRD status → `shipped` only after MVP acceptance metrics in PRD are met; plan → `done`
