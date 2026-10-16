# PRD-41 — Rich visual pass from the chosen Stitch screens

- **Status**: approved
- **GitHub**: [#41](https://github.com/chatondearu/nomorecallcenter/issues/41)
- **Date**: 2026-10-16
- **Tags**: #product #design

## Problem

The first MD3 pass ([PRD-31](PRD-31-md3-ui-pass.md)) shipped working French screens, but they stay visually thin next to the Stitch references. The same kind of element also drifts between those references.

## Goals

- [ ] Admin, the public site, and Android follow the Stitch hierarchy using one chosen variant per element.
- [ ] Shared web pieces are reused. A pattern is not restyled per page.
- [ ] Product behavior from PRD-28 and ADR-010 stays as it is.

## Non-goals

- Pasting `design/stitch-reference/*/code.html`
- Inter, portrait images, or Reka UI
- New screening, sync, or privacy behavior
- Screens the product does not have: retention settings, cluster, ARCEP browser, emergency intercept
- Product myths: offline-only SQLite, federation, open signup, "sans compte", "100% hors ligne"

## Canonical variants

| Element | Keep | Drop |
| --- | --- | --- |
| Protection card | Home: shield, "Protection active", "Rejet silencieux" | Offline badge, SQLite version, latency |
| Android chrome | Bottom nav: Accueil, Règles, Statistiques, Paramètres | Headers that replace navigation |
| Activity row | Home row: icon, reason, time, status chip. On device, show the caller number | A second journal style |
| TTL chips | `1h` `24h` `7j` `30j` `Permanent`; active chip uses primary-container | A second chip style |
| Rule form | Back, number field, label, TTL, save. Allow and block stay | Regex, propose-to-global, federated instance |
| Stats | Big numbers, 7-day bars, top prefixes, same activity row | "SQLite embarqué" |
| Admin shell | Sidebar: Vue d'ensemble, Modération, Membres, Événements | P2P, cluster, emergency intercept, retention |
| Admin overview | Card grid with real counters only | Federated fingerprints, invented splits |
| Site | Hero "Un téléphone silencieux, un contrôle limpide.", phone mock, three steps, F-Droid and self-host | False product claims |

## Users & scenarios

| Persona | Scenario |
| ------- | -------- |
| End user | Opens a French app whose home, rules, stats, and settings match the chosen screens. |
| Visitor | Reads a landing with a phone mock and two real doc links. |
| Instance admin | Uses one sidebar, moderates, invites, and reveals one event. |

## Requirements

### Functional

- [ ] Web styling uses Uno token aliases. No new ad-hoc CSS file. No raw hex in templates. Roboto Flex, not Inter.
- [ ] The shared layer owns the protection card, activity row, TTL chips, stat tile, filled button, and outlined field.
- [ ] Admin approve, reject, invite, and reveal keep the current API calls.
- [ ] Android matcher tests and a debug build pass. Screening decisions do not change.

### Non-functional

- [ ] [#42](https://github.com/chatondearu/nomorecallcenter/issues/42) lands before [#44](https://github.com/chatondearu/nomorecallcenter/issues/44) and [#45](https://github.com/chatondearu/nomorecallcenter/issues/45).
- [ ] [#43](https://github.com/chatondearu/nomorecallcenter/issues/43) does not wait on the Vue kit.

## Related project memory

- ADRs: [[../foam/decisions/ADR-009-design-system|ADR-009]], [[../foam/decisions/ADR-010-caller-number-storage|ADR-010]], [[../foam/decisions/ADR-003-android-client-stack|ADR-003]]
- Brief: [`DESIGN.md`](../DESIGN.md)
- Reference only: `design/stitch-reference/`
- Previous pass: [[PRD-31-md3-ui-pass|PRD-31]]
- Behavior: [[PRD-28-android-allow-and-screening-modes|PRD-28]]

## Open questions

None. The user approved this breakdown on 2026-10-16.

## Success metrics

- A reader can match each surface to the chosen Stitch screen without seeing a second style for the same element.
- No Stitch myth appears in the UI copy.
- Admin moderation and reveal still work.

## Implementation pointer

- [[../plans/PLAN-42-shared-visual-kit|PLAN-42]]
- [[../plans/PLAN-43-android-stitch-screens|PLAN-43]]
- [[../plans/PLAN-44-admin-stitch-shell|PLAN-44]]
- [[../plans/PLAN-45-site-stitch-hero|PLAN-45]]
