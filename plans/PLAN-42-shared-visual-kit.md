# PLAN-42 — Shared visual kit

- **Status**: draft
- **PRD**: [[../prd/PRD-41-rich-visual-pass|PRD-41]]
- **GitHub**: [#42](https://github.com/chatondearu/nomorecallcenter/issues/42)

## Summary

Rebuild the shared presentational components so later admin and site work share one visual for each pattern in PRD-41.

## Constraints

| ADR | Constraint |
| --- | ---------- |
| [[../foam/decisions/ADR-009-design-system\|ADR-009]] | Uno aliases, Roboto Flex, no Stitch HTML paste, no Inter |
| [[../foam/decisions/ADR-010-caller-number-storage\|ADR-010]] | A row may show a hash or a number. The component does not decide server policy |

## Tasks

- [ ] Protection card shows a shield, "Protection active", and a "Rejet silencieux" pill. No offline or SQLite copy.
- [ ] Activity row shows an icon, a title, a reason, a time, and one status chip.
- [ ] TTL chips render `1h` `24h` `7j` `30j` `Permanent` with the active chip on primary-container.
- [ ] Stat tile, filled button, and outlined field stay on the same token aliases and gain the Stitch density (16px card radius, label scale).
- [ ] Admin and the public app still typecheck against the updated components.

## Test plan

- [ ] `pnpm --filter @nmcc/shared typecheck` or the consumer typecheck if the shared package has none.
- [ ] Rendered markup for the protection card contains "Protection active" and "Rejet silencieux".

## Verification

- [ ] No new CSS file and no raw hex in the Vue templates.
