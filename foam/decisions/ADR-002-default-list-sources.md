# ADR-002 — Default FR list sources and licensing

- **Status**: accepted
- **Date**: 2026-10-13
- **Tags**: #data #license #mvp #explore

## Context

MVP needs default French call-center / demarchage ranges under our CC BY-SA list policy (ADR-001), without illegally republishing third-party community datasets.

## Decision

1. **Default seed** = lists we **generate** from **ARCEP / data.gouv** numbering open data (**Licence Ouverte 2.0**), with source and date attribution, published as **CC BY-SA**.
2. **Do not vendor** Saracroche, Begone, or PhoneBlock datasets in the repository as our defaults.
3. Third-party blocklist APIs (e.g. PhoneBlock) may be **optional runtime** integrations later, never the canonical CC BY-SA seed.

## Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Vendor Saracroche lists | No clear data license grant; includes user reports |
| Vendor Begone XML | Repo without LICENSE → all rights reserved |
| Republish PhoneBlock dump as CC BY-SA | No republish grant; GPL code ≠ CC BY-SA data |

## Consequences

- **Positive**: Clean legal posture for OSS defaults; aligned with ADR-001.
- **Negative**: We own the pipeline to refresh ARCEP-derived ranges.
- **Constraint**: Attribution (LO 2.0) required on shipped/derived lists.

## References

- [Research: Redistribution rights…](https://github.com/chatondearu/nomorecallcenter/issues/4)
- [[ADR-001-mvp-product-constraints|ADR-001]]
