# ADR-007 — Temporary vs permanent personal blocks (MVP)

- **Status**: accepted
- **Date**: 2026-10-13
- **Tags**: #product #android #mvp #explore

## Context

Users need one-off spam blocks with duration (or forever), synced across devices on the same account, without auto-poisoning the shared instance list (ADR-001).

## Decision

1. **Personal rules** (per account): exact number or prefix; applied immediately in Call Screening.
2. **TTL presets**: 1h / 24h / 7d / 30d / permanent. Server is source of truth; sync to all devices of the account.
3. **Lifecycle**: renew (restart preset from now), convert to permanent, delete; on expiry the rule stops blocking.
4. **Global push**: from a personal rule or recent hit, CTA **« Proposer l'ajout à la liste globale »** creates a **moderated proposal** — never auto-publishes to the instance list.

## Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Personal block auto-adds to shared list | Violates admin-moderated trust model |
| Free-form end datetime only | Presets are enough for MVP |
| Device-local-only rules | Breaks multi-device value of the product |

## Consequences

- **Positive**: Immediate relief + optional community contribution path.
- **Negative**: Need sync + expiry job/checks on server and client.
- **Constraint**: Shared list changes only via proposal → admin approve.

## References

- [Decide: Temporary vs permanent block model](https://github.com/chatondearu/nomorecallcenter/issues/9)
- [[ADR-001-mvp-product-constraints|ADR-001]]
