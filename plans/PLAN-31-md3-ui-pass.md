# PLAN-31 — MD3 UI pass for admin, site, and Android

- **Status**: draft
- **PRD**: [[../prd/PRD-31-md3-ui-pass|PRD-31]]
- **GitHub**: [#31](https://github.com/chatondearu/nomorecallcenter/issues/31)
- **Date**: 2026-10-16

## Summary

Restyle admin, the public landing, and the Android app from `DESIGN.md` and ADR-009. Shared Vue components first. Do not copy Stitch HTML. Do not change screening or reveal rules.

## Constraints (from ADRs)

| ADR | Constraint |
| --- | ---------- |
| [[../foam/decisions/ADR-009-design-system\|ADR-009]] | MD3 tokens, Uno on web, Roboto Flex, Compose on Android. Stitch is reference only. |
| [[../foam/decisions/ADR-010-caller-number-storage\|ADR-010]] | Server UI shows a hash. Reveal is an explicit admin action. The device journal may show the caller number. |
| [[../foam/decisions/ADR-003-android-client-stack\|ADR-003]] | Kotlin, Compose, Material 3. |

Do not violate accepted ADRs without superseding them.

## Scope

### In scope

- Shared components listed in the PRD
- Admin, landing, and Android screen restyle
- French copy

### Out of scope

- Reka and Histoire until a primitive is required
- Product behavior changes

## Technical approach

Add presentational Vue components to the shared layer using existing Uno aliases (`bg-primary`, `text-on-surface`, `bg-system-surface`, status tokens). Admin and the public app consume them. Android keeps logic in the current screens and only changes Material 3 layout and French strings. One child issue per slice.

## Tasks

### Slice 1 — Shared web components ([#32](https://github.com/chatondearu/nomorecallcenter/issues/32))

- [ ] Protection card, stat tile, event row, TTL chips, effect chips, filled button, outlined field.
- [ ] No raw hex and no new CSS file.
- [ ] A page in admin or the public app renders each component.

### Slice 2 — Admin ([#33](https://github.com/chatondearu/nomorecallcenter/issues/33))

- [ ] Overview, proposals (Approuver / Rejeter), invites, and the feed use the shared pieces.
- [ ] Feed shows the hash. Révéler still calls the existing admin reveal endpoint.
- [ ] Blocked by slice 1.

### Slice 3 — Public landing ([#34](https://github.com/chatondearu/nomorecallcenter/issues/34))

- [ ] Hero, three steps, self-host and F-Droid links. French.
- [ ] No SQLite, federation, or open-signup claims.
- [ ] Blocked by slice 1.

### Slice 4 — Android ([#35](https://github.com/chatondearu/nomorecallcenter/issues/35))

- [ ] French Material 3 home, rules (allow/block + TTL), stats, and settings.
- [ ] Settings include instance URL, contacts-only, hidden numbers, notification default off.
- [ ] Matcher tests still pass. Screening decisions unchanged.

## Risks & mitigations

| Risk | Mitigation |
| ---- | ---------- |
| Stitch copy contradicts the product | ADR-009 and this PRD override the HTML |
| Restyle breaks reveal or approve | Keep the current API calls; change presentation only |

## Test plan

- [ ] Admin: sign in, approve or reject a proposal, reveal one event
- [ ] Landing: read the three steps and both outbound links
- [ ] Android: `:matcher:test` and a debug build

## Verification

- [ ] Visual check of admin and the landing in the browser
- [ ] `cd android && ./gradlew :matcher:test`
