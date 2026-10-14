# ADR-009 — Design system (MD3 web + Android)

- **Status**: accepted
- **Date**: 2026-10-14
- **Tags**: #design #md3 #nuxt #android #explore

## Context

Explore map lacked a UI system. Stitch (Google Labs) produced a first MD3-oriented pack (colors, screens for app / site / admin) with useful structure but **cross-page component drift** and some **product-inaccurate** copy (offline SQLite, federation, etc.).

## Decision

1. **Canonical brief**: root [`DESIGN.md`](../../DESIGN.md) + [`design/tokens/md3-colors.json`](../../design/tokens/md3-colors.json).
2. **Seed**: `#1A6352` (forest pine), MD3 tonal pipeline (BAJO-like `material-color-utilities` in shared UnoCSS layer).
3. **Web**: Nuxt shared layer · UnoCSS only · Reka UI first · Histoire catalog (isolated).
4. **Android**: Compose Material 3; same role names; export tokens at build time from the shared seed/JSON.
5. **Typography**: Roboto / Roboto Flex — **not** Stitch’s Inter.
6. **Stitch export**: kept under `design/stitch-reference/` as **IA/reference only** — never the implementation source of truth.
7. **Agent guardrails**: `.cursor/rules/design-system.mdc`.

## Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Implement by copying Stitch HTML | Inconsistent components across pages |
| Inter everywhere | Conflicts with project anti-default-stack preference; Android MD3 is Roboto-native |
| Separate unrelated palettes per surface | Breaks brand coherence app/site/admin |

## Consequences

- **Positive**: One token language; agents have explicit rules; Stitch inspiration preserved.
- **Negative**: Need a small token→Android export step when the monorepo lands.
- **Constraint**: UI PRs that introduce raw hex or extra CSS files need justification in the PR.

## References

- [Decide: Design system MD3 web + Android alignment](https://github.com/chatondearu/nomorecallcenter/issues/12)
- Stitch pack: `design/stitch-reference/`
- [[ADR-003-android-client-stack|ADR-003]], [[ADR-004-backend-stack|ADR-004]]
