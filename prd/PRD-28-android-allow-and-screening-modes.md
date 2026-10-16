# PRD-28 — Android allow rules, local screening modes, and admin number reveal

- **Status**: approved
- **GitHub**: [#28](https://github.com/chatondearu/nomorecallcenter/issues/28)
- **Date**: 2026-10-16
- **Tags**: #product #android #privacy

## Problem

Blocking is one-way. A shared list or a personal block cannot be overridden by an allow, the phone cannot be limited to contacts, and hidden numbers always ring. The local history does not show which number was rejected. On the server, ADR-001 and ADR-006 store only a prefix or hash, so an instance admin cannot inspect the number that was actually screened.

## Goals

- [ ] Personal **allow** rules, synced like personal blocks, with the same TTL presets. An active allow wins over any block.
- [ ] Device-local **contacts only** and **block hidden numbers**. Contacts never leave the phone.
- [ ] A local journal of blocked calls that shows the caller number (the dialer already has it).
- [ ] Optional local “call blocked” notification, **off** by default. Silent reject stays the default.
- [ ] Server stores the clear normalized number on call events. Ordinary API responses show a hash. Only the instance admin can reveal the clear number, via an explicit action.

## Non-goals

- SMS filtering changes
- Trailing `#` wildcard patterns and indexed pattern lookup
- Content-hash list sync, battery-optimization prompts, work-profile handling
- Saracroche organization cloud, MDM, or vendoring their lists (ADR-002)
- Changing silent reject as the default block action (ADR-001)

## Users & scenarios

| Persona | Scenario |
| ------- | -------- |
| End user | Adds an allow for a number inside a blocked range; the call rings. Turns on contacts-only; unknown numbers are rejected, allows still ring. Turns on hidden-number blocking; calls with no handle are rejected. Opens the on-device journal and sees the caller number. Leaves the blocked-call notification off. |
| Instance member | Sees personal and instance stats as hashes and prefixes. Cannot reveal another user’s number, or their own, through the API. |
| Instance admin | Sees hashes in the event feed, then explicitly reveals one clear number. |
| Self-host operator | Knows database backups contain clear numbers. Retention still drops old events. |

## Requirements

### Functional — screening order

On an incoming call, decide in this order:

1. No caller number: **block** if “block hidden numbers” or “contacts only” is on; otherwise allow.
2. Active personal **allow** matches (exact or prefix, TTL not expired): **allow**. This wins over shared-list blocks, personal blocks, and contacts-only.
3. “Contacts only” is on and the number is not a device contact: **block**.
4. Shared list or active personal **block** matches: **block** (silent reject: disallow, reject, skip call log, skip system notification).
5. Otherwise **allow**.

Shared lists stay block-only. Allow rules are personal rules on the account (ADR-007 presets: 1h / 24h / 7d / 30d / permanent), synced from the server.

### Functional — device

- [ ] Contacts-only and hidden-number flags are device preferences. Do not upload the contact list.
- [ ] Local journal records blocked calls: caller number, matched pattern when any, action, time. Show the caller number for shared-list blocks and personal blocks.
- [ ] Optional notification after a block, default off. When off, behavior stays today’s silent reject. When on, still silent-reject the call and post an app notification.

### Functional — server

- [ ] Ingest stores the clear normalized number with the event.
- [ ] Member APIs, aggregates, and the default admin feed return a display hash (SHA-256 hex of the normalized digits), not the clear number.
- [ ] Instance admin only: an explicit reveal action returns the clear number for one event.
- [ ] Existing retention still deletes old events, clear number included.

### Non-functional

- [ ] A database dump contains clear numbers. Document that next to the deploy notes when this ships.
- [ ] Reveal is not a list endpoint and is not available to members.

## Related project memory

- ADRs: [[../foam/decisions/ADR-010-caller-number-storage|ADR-010]] supersedes the hash-only storage point of [[../foam/decisions/ADR-001-mvp-product-constraints|ADR-001]] and [[../foam/decisions/ADR-006-mvp-stats-surface|ADR-006]]
- Allow TTL: [[../foam/decisions/ADR-007-block-ttl-model|ADR-007]]
- Lists: [[../foam/decisions/ADR-002-default-list-sources|ADR-002]]
- Client: [[../foam/decisions/ADR-003-android-client-stack|ADR-003]]
- Product: [[../foam/product/vision|Vision]]

## Open questions

None. Locked in conversation on 2026-10-16 (allow-wins, personal allow TTL, device-local modes, journal shows the caller number, notification off by default, admin reveal, SMS and Saracroche extras out of scope).

## Success metrics

- A number on the shared list rings when an active personal allow matches, including in contacts-only mode.
- A hidden number is rejected only when that device setting is on.
- Member `GET` of events or stats never includes a clear number; an admin reveal on one event does.
- With the notification pref off, a block still does not ring.

## Implementation pointer

[[../plans/PLAN-28-android-allow-and-screening-modes|PLAN-28]] (`plans/PLAN-28-android-allow-and-screening-modes.md`).
