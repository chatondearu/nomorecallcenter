# PLAN-43 — Android Stitch screens

- **Status**: draft
- **PRD**: [[../prd/PRD-41-rich-visual-pass|PRD-41]]
- **GitHub**: [#43](https://github.com/chatondearu/nomorecallcenter/issues/43)

## Summary

Restyle the Compose screens to the chosen Stitch hierarchy without changing screening.

## Constraints

| ADR | Constraint |
| --- | ---------- |
| [[../foam/decisions/ADR-003-android-client-stack\|ADR-003]] | Kotlin, Compose, Material 3 |
| [[../foam/decisions/ADR-009-design-system\|ADR-009]] | Same color roles as the token file. No Inter |
| [[../foam/decisions/ADR-010-caller-number-storage\|ADR-010]] | The device journal shows the caller number |

## Tasks

- [ ] Every tab uses the bottom nav: Accueil, Règles, Statistiques, Paramètres.
- [ ] Home shows the chosen protection card and the activity row.
- [ ] Rules list and the rule form share one TTL chip row. Allow and block remain.
- [ ] Stats show big numbers, seven daily bars, top prefixes, and the same activity row.
- [ ] Settings still edit instance URL, contacts-only, hidden numbers, and the notification toggle (default off).
- [ ] Matcher tests pass and a debug APK builds.

## Test plan

- [ ] `./gradlew :matcher:test :app:assembleDebug` from `android/`.

## Verification

- [ ] No new screening branch and no new preference.
