# PRD-1 — nomorecallcenter MVP + vision

- **Status**: approved
- **GitHub**: [#1](https://github.com/chatondearu/nomorecallcenter/issues/1)
- **Date**: 2026-10-13
- **Tags**: #product #mvp #android #self-host

## Problem

Call centers and spam callers often rotate through **number ranges**. Blocking numbers one-by-one is endless. People need:

1. **Prefix/range filtering** on Android without becoming the default dialer.
2. **Personal blocks** that can be temporary or permanent, synced across devices.
3. A path to contribute to a **shared list** without auto-poisoning it.
4. **Stats** (personal and instance-level) so patterns are visible and moderation is informed.
5. An **open-source, self-hosted** multi-user backend they control — not a proprietary cloud.

Explore map [#1](https://github.com/chatondearu/nomorecallcenter/issues/1) locked the product and tech constraints in ADR-001…008. This PRD is the dual handoff: **normative MVP** + **non-normative vision**.

## Goals

### A — MVP (normative)

- [ ] Ship an Android app that screens inbound calls by **prefix/range** and personal rules via `CallScreeningService` + `ROLE_CALL_SCREENING` (silent reject default).
- [ ] Ship a **self-hostable** multi-user backend (BAJO-shaped monorepo) with invite-only auth, shared FR default lists, moderated proposals, personal TTL rules sync, and analytics ingestion.
- [ ] Ship a **Nuxt admin** for moderation, instance stats, proposal funnel, and admin-only near-live event debug feed.
- [ ] Distribute Android via **GitHub Releases + F-Droid**; backend via **OCI + docker compose**.
- [ ] Publish default FR list seed from **ARCEP / data.gouv** (LO 2.0 → CC BY-SA), not vendored third-party community dumps.

### B — Vision note (non-normative, 12–24 months)

- [ ] Document direction for EU lists, Play Store, ML/scoring, federation, iOS, SMS/RCS, STIR/SHAKEN, OIDC, GraphQL — **not** MVP acceptance criteria.

## Non-goals

### MVP

- Default dialer / SMS app architecture
- Worldwide default blocklists on day one
- Auto-publish of unmoderated community spam
- Proprietary hosted cloud as the only deploy model
- Vendoring Saracroche / Begone / PhoneBlock datasets as our CC BY-SA defaults
- Flutter/RN client; Go/Rust backend
- GraphQL transport; open public self-registration
- Member-facing live dashboards, BI, or ops Grafana
- Play Store listing

### Explicitly deferred (vision)

- ML reputation / auto-scoring
- Instance federation
- iOS and non-Android clients
- SMS / RCS blocking
- STIR/SHAKEN enrichment
- OIDC / SSO / magic-link
- GraphQL if REST proves insufficient

## Users & scenarios

| Persona | Scenario |
| ------- | -------- |
| **End user (Android)** | Installs from F-Droid/GitHub, picks instance URL, accepts invite, grants Call Screening role, gets silent reject on FR call-center ranges + personal TTL blocks; sees personal counters and top prefixes. |
| **Instance member** | Same as end user; sees **instance aggregate** stats (not other users’ private detail); can propose a rule to the global list. |
| **Instance admin** | Bootstraps as first account (or `BOOTSTRAP_ADMIN_EMAIL`); issues invites; approves/rejects proposals; watches proposal funnel + near-live event feed for debug. |
| **Self-host operator** | Runs Postgres + app/admin via docker compose; configures retention; serves optional public demo separately from personal prod. |

## Requirements

### Functional — Android (ADR-003, ADR-007, ADR-008)

- [ ] Kotlin + Jetpack Compose client, `minSdk` 29.
- [ ] Implement `CallScreeningService`; request and hold `ROLE_CALL_SCREENING`; onboarding refuses “protected” claim until role held.
- [ ] Match inbound handles by **prefix/range** (default FR seed) and **personal rules** (exact or prefix).
- [ ] Default action: **silent reject** (no ring).
- [ ] Personal rules: TTL presets **1h / 24h / 7d / 30d / permanent**; renew / make permanent / delete; expiry stops blocking.
- [ ] Sync personal rules from server (account source of truth) across devices.
- [ ] CTA **“Proposer l'ajout à la liste globale”** creates a moderated proposal (no auto-publish).
- [ ] Stats UI: personal counters (screened/blocked/allowed, 7d/30d), top prefixes, recent personal history (prefix/hash, action, time).
- [ ] Configure **instance base URL**; authenticate with email/password (Better Auth session/token).

### Functional — Backend & admin (ADR-004, ADR-005, ADR-006, ADR-002)

- [ ] pnpm monorepo `modules/*`: `db` (Postgres + Drizzle), Nuxt/Nitro **REST** API, Nuxt **admin**, `shared`; Android tree in-repo but outside Node runtime.
- [ ] Better Auth + Drizzle: first account on empty instance = admin (optional `BOOTSTRAP_ADMIN_EMAIL`); **invite-only** join; email + password.
- [ ] Default list pipeline: generate/update from ARCEP/data.gouv (LO 2.0) with attribution; publish as CC BY-SA.
- [ ] Shared list changes only via **proposal → admin approve/reject**.
- [ ] Ingest call events: prefix and/or hash, timestamp, action, user id — **no clear full numbers**; configurable retention.
- [ ] Instance aggregate stats for members; admin: aggregates + proposal funnel + event volume + **admin-only near-real-time debug feed** (poll/SSE).
- [ ] Docker images (e.g. GHCR) + compose example for Postgres + services.

### Non-functional

- [ ] Licenses: **AGPL-3.0** (code), **CC BY-SA** (our list datasets).
- [ ] Privacy-by-design: hash/prefix storage; no member feed of others’ identifiable call detail.
- [ ] Screening response within Android’s Call Screening time budget (~5s).
- [ ] Self-host docs sufficient for a single-node compose deploy.
- [ ] F-Droid-friendly Android build (no Play-only dependencies required for MVP).

## Vision (non-normative)

Sketch for the Vision section of product memory / future PRDs — **not** MVP gates:

| Theme | Direction |
| ----- | --------- |
| Lists | EU (non-FR) default ranges; richer community signals under moderation |
| Distribution | Google Play if policy allows; broader sideload UX |
| Intelligence | Scoring / ML suggestions feeding human moderation — not auto-publish |
| Topology | Optional federation between instances |
| Clients | iOS / other platforms if APIs allow analogous screening |
| Channels | SMS/RCS; STIR/SHAKEN enrichment |
| Auth | OIDC/SSO/magic-link |
| API | GraphQL only if Android REST over/under-fetch becomes painful |
| Ops | Stronger monitoring beyond admin event feed |

## Related project memory

- Explore map: [#1](https://github.com/chatondearu/nomorecallcenter/issues/1)
- ADRs:
  - [[../foam/decisions/ADR-001-mvp-product-constraints|ADR-001]] MVP product constraints
  - [[../foam/decisions/ADR-002-default-list-sources|ADR-002]] Default FR list sources
  - [[../foam/decisions/ADR-003-android-client-stack|ADR-003]] Android client stack
  - [[../foam/decisions/ADR-004-backend-stack|ADR-004]] Backend stack (BAJO-shaped)
  - [[../foam/decisions/ADR-005-auth-model|ADR-005]] Auth model
  - [[../foam/decisions/ADR-006-mvp-stats-surface|ADR-006]] Stats surface
  - [[../foam/decisions/ADR-007-block-ttl-model|ADR-007]] Personal block TTL
  - [[../foam/decisions/ADR-008-distribution-channels|ADR-008]] Distribution channels
- Product: [[../foam/product/vision|Vision & positioning]]

## Open questions

1. Exact hash algorithm and prefix granularity for storage/matching (implementation detail for plan).
2. Whether admin and API ship as one Nuxt process or two images (BAJO often splits — choose in `cda-plan`).
3. F-Droid submission path (official repo vs own repo) — operational, not product-blocking.
4. Public demo instance hosting and data policy (synthetic vs opt-in) — confirm before first public deploy.

## Success metrics

- User can grant Call Screening and block a known FR call-center prefix with silent reject on a physical device (API 29+).
- Second device on the same account receives personal TTL rules via sync within a short interval after create/renew/delete.
- Member proposal appears in admin queue; approve updates shared list for all instance clients; reject does not.
- Fresh compose stack: bootstrap admin → invite user → Android login against that instance URL.
- Default list build from ARCEP/data.gouv runs with attribution and no vendored third-party dumps.

## Implementation pointer

`plans/PLAN-1-mvp-and-vision.md` from `cda-plan` (create after this PRD is **approved**).
