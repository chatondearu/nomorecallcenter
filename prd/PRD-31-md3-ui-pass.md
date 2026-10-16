# PRD-31 — MD3 UI pass for admin, site, and Android

- **Status**: approved
- **GitHub**: [#31](https://github.com/chatondearu/nomorecallcenter/issues/31)
- **Date**: 2026-10-16
- **Tags**: #product #design

## Problem

The three surfaces work, but they do not yet look like the design brief. Admin lists are plain, the public app is an API sentence, and much of the Android copy is still English. Stitch HTML exists only as layout reference and must not be copied.

## Goals

- [ ] Shared web components (protection card, stat tile, event row, TTL and effect chips, filled button, outlined field) using MD3 Uno tokens.
- [ ] Admin restyle: overview, proposal queue, invites, feed with hash and an explicit admin reveal.
- [ ] French public landing: hero, three steps, self-host and F-Droid.
- [ ] Android screens in French on Compose Material 3, same role names, PRD-28 behavior unchanged.

## Non-goals

- Installing Reka UI or Histoire until a dialog, menu, or tabs primitive is required
- Copying Stitch HTML, Inter, or portrait images
- Offline-only SQLite, federation, or open signup
- New screening or privacy behavior beyond PRD-28 and ADR-010

## Users & scenarios

| Persona | Scenario |
| ------- | -------- |
| End user | Opens the app in French, sees whether protection is on, manages allow and block rules, reads stats. |
| Visitor | Reads the landing and finds self-host plus F-Droid without a false product claim. |
| Instance admin | Moderates proposals and reveals one number from a feed that otherwise shows a hash. |

## Requirements

### Functional

- [ ] Web styling is Uno theme aliases only. No new ad-hoc CSS file. No raw hex in templates.
- [ ] Shared components live in the Nuxt shared layer and are used by admin and the public site.
- [ ] Admin reveal stays an explicit action. Other server surfaces show the hash.
- [ ] Android copy is French. Settings cover instance URL, contacts-only, hidden numbers, and the blocked-call notification (default off).

### Non-functional

- [ ] Issue [#32](https://github.com/chatondearu/nomorecallcenter/issues/32) lands before admin ([#33](https://github.com/chatondearu/nomorecallcenter/issues/33)) and the site ([#34](https://github.com/chatondearu/nomorecallcenter/issues/34)).
- [ ] Android ([#35](https://github.com/chatondearu/nomorecallcenter/issues/35)) does not wait on the Vue components.

## Related project memory

- ADRs: [[../foam/decisions/ADR-009-design-system|ADR-009]], [[../foam/decisions/ADR-010-caller-number-storage|ADR-010]], [[../foam/decisions/ADR-003-android-client-stack|ADR-003]]
- Brief: [`DESIGN.md`](../DESIGN.md)
- Behavior: [[PRD-28-android-allow-and-screening-modes|PRD-28]]

## Open questions

None. Order and component list were set in `DESIGN.md` on 2026-10-16.

## Success metrics

- Admin and the landing use the shared components and the MD3 palette (primary `#004a3c` / seed `#1A6352`).
- A French reader can use the Android home, rules, stats, and settings screens without English labels.
- Approving or revealing from the restyled admin still matches the current API.

## Implementation pointer

[[../plans/PLAN-31-md3-ui-pass|PLAN-31]]
