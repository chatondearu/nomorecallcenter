# ADR-001 — MVP product constraints (explore grilling)

- **Status**: accepted
- **Date**: 2026-10-13
- **Tags**: #product #mvp #explore

## Context

Exploration of nomorecallcenter locked product constraints before a PRD. Source: cda-explore grilling rounds 1–2.

## Decision

1. **Handoff target**: one dual deliverable — normative **MVP PRD** + non-normative **product vision** (12–24 months).
2. **Topology**: multi-user from MVP; each user picks a **self-hosted** server; plus a **public demo** instance for onboarding (not a proprietary cloud).
3. **Backend responsibilities**: default/shared lists, spam/range **proposals**, moderation, and **analytics** that power suggestions — not lists-only.
4. **Default lists**: **France first** (ARCEP / known call-center ranges); EU later.
5. **Trust**: any authenticated user may **propose**; **instance admin** publishes to the shared list (no auto-publish).
6. **Privacy**: store **prefix and/or hash**, timestamp, call action, user id — not clear full numbers; retention configurable per instance. **Superseded 2026-10-16 by [[ADR-010-caller-number-storage|ADR-010]]** for call events: the clear normalized number is stored so an instance admin can reveal it; members still receive a hash.
7. **License**: code **AGPL-3.0**; list datasets **CC BY-SA** (or equivalent) when we publish our own.
8. **Block UX default**: **silent reject** via Android Call Screening (no ring).

## Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Solo / local-only stats first | User requires multi-user + server analytics from MVP |
| Auto-publish community spam | Poison risk on shared lists |
| World-wide default lists day 1 | Too broad for MVP |
| Proprietary hosted-only SaaS | Contradicts self-host OSS goal |

## Consequences

- **Positive**: Clear scope for PRD; privacy and trust model fixed early.
- **Negative**: Multi-tenant auth + moderation required in MVP (higher complexity).
- **Constraint**: Client must implement prefix matching in `CallScreeningService` (system blocklist is exact-match only).

## References

- Repo: https://github.com/chatondearu/nomorecallcenter
- Map: [Explore: nomorecallcenter MVP + vision](https://github.com/chatondearu/nomorecallcenter/issues/1)
