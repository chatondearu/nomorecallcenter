# PLAN-45 — Public Stitch landing

- **Status**: draft
- **PRD**: [[../prd/PRD-41-rich-visual-pass|PRD-41]]
- **GitHub**: [#45](https://github.com/chatondearu/nomorecallcenter/issues/45)

## Summary

Replace the landing with the chosen public screen. Blocked by the shared kit issue.

## Constraints

| ADR | Constraint |
| --- | ---------- |
| [[../foam/decisions/ADR-009-design-system\|ADR-009]] | Uno aliases, French, no Stitch myths, no Inter |
| [[../foam/decisions/ADR-008-distribution-channels\|ADR-008]] | F-Droid notes and self-host docs are the outbound links |

## Tasks

- [ ] Hero reads "Un téléphone silencieux, un contrôle limpide."
- [ ] A phone mock shows one silently rejected call using the shared activity row or protection card.
- [ ] Three steps stay, with F-Droid and self-host links to the existing docs.
- [ ] Copy does not claim offline-only, SQLite, federation, or open signup.

## Test plan

- [ ] `pnpm --filter @nmcc/app typecheck`
- [ ] Served HTML contains the hero sentence and both doc links.

## Verification

- [ ] No new CSS file and no raw hex.
