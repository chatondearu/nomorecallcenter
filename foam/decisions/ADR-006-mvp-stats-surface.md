# ADR-006 — MVP stats and analytics surface

- **Status**: accepted
- **Date**: 2026-10-13
- **Tags**: #product #analytics #mvp #explore

## Context

ADR-001 requires server-side analytics (hash/prefix, timestamp, action, user id) to power lists and moderation. Need to bound what the MVP shows to users vs admins vs vision.

## Decision

1. **User (Android)**: personal counters (screened / blocked / allowed over 7d & 30d), top prefixes, recent personal history (prefix/hash, action, time). No fancy charts.
2. **Instance members**: same metrics as **aggregates across the instance** (no per-other-user detail).
3. **Admin**: instance aggregates + **proposal funnel** (submitted / approved / rejected) + event volume; plus an **admin-only near-real-time debug feed** of recent instance events (prefix/hash, action, user id, time) via short poll or SSE — not infra monitoring.
4. **Vision / later**: ML reputation scores, push “hot range” alerts, BI exports, member-facing live dashboards, ops/Grafana-style monitoring.

Privacy: no clear phone numbers; no identifiable “who got which full number” feed for members.

## Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Member-facing live dashboard | Debug noise for end users; privacy surface |
| Full ops monitoring in MVP | Out of product MVP scope |
| ML scoring in MVP | Fog; human moderation first |

## Consequences

- **Positive**: Clear UI scope; admin can verify screening/sync; members get useful counts.
- **Negative**: Admin live feed needs a small realtime/poll path in Nitro.
- **Constraint**: Aggregates and feeds must honor hash/prefix-only storage (ADR-001).

## References

- [Decide: MVP stats and analytics surface](https://github.com/chatondearu/nomorecallcenter/issues/8)
- [[ADR-001-mvp-product-constraints|ADR-001]]
