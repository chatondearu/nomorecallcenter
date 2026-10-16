# PLAN-44 — Admin Stitch shell

- **Status**: draft
- **PRD**: [[../prd/PRD-41-rich-visual-pass|PRD-41]]
- **GitHub**: [#44](https://github.com/chatondearu/nomorecallcenter/issues/44)

## Summary

Apply the shared kit inside one admin shell. Blocked by the shared kit issue.

## Constraints

| ADR | Constraint |
| --- | ---------- |
| [[../foam/decisions/ADR-009-design-system\|ADR-009]] | Uno aliases, French, no Stitch HTML paste |
| [[../foam/decisions/ADR-010-caller-number-storage\|ADR-010]] | Feed shows a hash. Reveal is one explicit POST |

## Tasks

- [ ] Sidebar lists only Vue d'ensemble, Modération, Membres, Événements.
- [ ] Overview cards use counters the API already returns.
- [ ] Moderation still approves and rejects. Members still create an invite.
- [ ] The event feed uses the shared activity row and reveal stays a POST for one event.
- [ ] No fork of a shared component.

## Test plan

- [ ] `pnpm --filter @nmcc/admin typecheck`

## Verification

- [ ] Approve, reject, invite, and reveal URLs are unchanged.
