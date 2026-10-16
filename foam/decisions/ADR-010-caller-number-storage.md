# ADR-010 — Caller number storage and admin reveal

- **Status**: accepted
- **Date**: 2026-10-16
- **Tags**: #privacy #data #android

## Context

ADR-001 and ADR-006 store only a prefix or hash for call events, so an instance admin cannot see the number that was screened. The Android journal can show the caller number locally because the dialer already has it. Operators still need a server-side copy they can inspect without showing clear numbers to members.

## Decision

1. **Call events** persist the clear normalized number (digits, same canonical form as the matcher) plus timestamp, action, and user id.
2. **Display hash** for every non-reveal response is SHA-256 hex of that normalized digit string. Member stats, member history, and the default admin feed use this hash (and prefix aggregates), not the clear number.
3. **Reveal** is a separate instance-admin action on one event. It returns the stored clear number. Members cannot call it.
4. **Retention** still deletes aged events, including the clear number. Database backups contain clear numbers; operators must treat dumps as sensitive.
5. **On device**, the blocked-call journal may show the caller number for every block (personal rule or shared list). That display does not depend on reveal. Contacts stay on device.

This supersedes ADR-001 § privacy (“not clear full numbers”) and ADR-006’s hash-only storage constraint for the admin reveal path. Member-facing server surfaces stay hashed.

## Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Keep one-way hash only | Admin cannot recover the number |
| Encrypt at rest with a separate admin key | Extra key management for the same access rule |
| Hide the caller number in the local journal when a shared list matched | The number is already in the phone app |

## Consequences

- **Positive**: Admin can investigate one call; members still do not receive clear numbers from the API.
- **Negative**: A database dump leaks numbers. Reveal is a privileged read.
- **Constraint**: No member endpoint and no bulk reveal API.

## References

- [feat(android): allow rules, local screening modes, and admin number reveal](https://github.com/chatondearu/nomorecallcenter/issues/28)
- [[ADR-001-mvp-product-constraints|ADR-001]]
- [[ADR-006-mvp-stats-surface|ADR-006]]
- [[../../prd/PRD-28-android-allow-and-screening-modes|PRD-28]]
