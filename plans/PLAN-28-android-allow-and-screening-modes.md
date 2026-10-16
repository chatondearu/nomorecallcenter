# PLAN-28 — Android allow rules, local screening modes, and admin number reveal

- **Status**: in review
- **PRD**: [[../prd/PRD-28-android-allow-and-screening-modes|PRD-28]]
- **GitHub**: [#28](https://github.com/chatondearu/nomorecallcenter/issues/28)
- **Date**: 2026-10-16

## Summary

Personal allow rules sync with the same TTL presets as blocks and win over any block. The phone adds contacts-only and hidden-number settings, a local blocked-call journal that shows the caller number, and an optional blocked-call notification that stays off by default. Call events store the clear normalized number; APIs return a SHA-256 hash unless an instance admin reveals one event.

## Constraints (from ADRs)

| ADR | Constraint |
| --- | ---------- |
| [[../foam/decisions/ADR-001-mvp-product-constraints\|ADR-001]] | Silent reject stays the default block. Multi-user self-host. |
| [[../foam/decisions/ADR-002-default-list-sources\|ADR-002]] | Do not vendor Saracroche or other third-party dumps. |
| [[../foam/decisions/ADR-003-android-client-stack\|ADR-003]] | Kotlin, Compose, minSdk 29, `CallScreeningService`. |
| [[../foam/decisions/ADR-005-auth-model\|ADR-005]] | Reveal is instance-admin only. |
| [[../foam/decisions/ADR-006-mvp-stats-surface\|ADR-006]] | Member stats and the default admin feed stay hash/prefix. |
| [[../foam/decisions/ADR-007-block-ttl-model\|ADR-007]] | Allow rules use the same presets and server source of truth. |
| [[../foam/decisions/ADR-010-caller-number-storage\|ADR-010]] | Store the clear normalized number; display SHA-256 hex; explicit admin reveal of one event. |

Do not violate accepted ADRs without superseding them.

## Scope

### In scope

- Personal rule effect `allow` or `block`
- Device prefs: contacts only, block hidden numbers, blocked-call notification (default off)
- Local journal of blocked calls with the caller number
- Event storage of the clear number, hashed reads, admin reveal
- Deploy note that database dumps contain clear numbers

### Out of scope

- SMS changes, `#` wildcards, hash list sync, battery or work-profile prompts, Saracroche cloud/MDM

## Technical approach

Extend personal rules with an effect. Uniqueness is per user, pattern, match type, and effect, so the same pattern can be both an allow and a block. Screening order is the PRD order: hidden number, then allow, then contacts-only, then shared or personal block, then allow through. Call events gain a clear normalized number column; existing read models expose `numberHash` as SHA-256 of those digits and omit the clear value. Reveal is its own admin route for one event id.

## Tasks

### Slice 1 — Matcher

- [x] A personal rule carries effect `allow` or `block`, match type `exact` or `prefix`, and `expiresAt` (null = permanent).
- [x] Incoming decision follows the PRD order. An active allow wins over a shared-list hit, a personal block, and contacts-only.
- [x] No caller number blocks only when hidden-number blocking or contacts-only is on.
- [x] Unit tests cover allow-wins, expired allow, contacts-only with an allow, and a hidden number.

### Slice 2 — Rules API

- [x] Create and sync personal rules with effect `allow` or `block` and the existing TTL presets.
- [x] The same pattern may exist once as allow and once as block for one user.
- [x] Expired rules are not returned as active.
- [x] Acceptance: member creates an allow, second device syncs it, a non-owner cannot read it.

### Slice 3 — Events and reveal

- [x] Ingest stores the clear normalized number and the SHA-256 hex display hash.
- [x] Member history, aggregates, and the default admin feed do not include the clear number.
- [x] Instance admin can reveal one event and receive the clear number. A member receives a refusal.
- [x] Retention deletion removes the clear number with the event.
- [x] Acceptance script: two users ingest; member GET has no clear number; admin reveal returns it.

### Slice 4 — Android behavior

- [x] Synced allow rules participate in screening with the slice 1 order.
- [x] Contacts-only and hidden-number flags are device-local. The contact list is not uploaded.
- [x] After a block, the local journal shows the caller number, matched pattern when any, and time.
- [x] Notification pref defaults off. Off keeps silent reject with no app notification. On still silent-rejects and posts one local notification.
- [x] `:matcher:test` passes. Debug assemble still builds.

### Slice 5 — Operator note

- [x] Self-host docs state that database backups contain clear caller numbers.

## Risks & mitigations

| Risk | Mitigation |
| ---- | ---------- |
| Existing events have no clear number | Reveal returns empty for those rows; new events store it |
| Unique key today is user + pattern + match type | Include effect so allow and block can coexist |
| Contacts permission denial | Contacts-only cannot be on without the permission; screening does not upload contacts |

## Test plan

Seams:

- Pure matcher decision (JVM)
- Personal-rules sync JSON
- Event ingest and admin reveal HTTP
- Device prefs and journal are local; no contract test for the contact provider

- [x] Matcher unit tests for the screening order
- [x] Rules acceptance extended with an allow
- [x] Stats or events acceptance: hash on read, clear number on admin reveal only

## Verification

- [x] `nix develop -c` typecheck / affected acceptance scripts
- [x] `cd android && ./gradlew :matcher:test`
- [ ] Foam already has ADR-010; update PRD status to `shipped` when the issue closes
