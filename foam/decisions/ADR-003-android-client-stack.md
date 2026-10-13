# ADR-003 — Android client stack for MVP

- **Status**: accepted
- **Date**: 2026-10-13
- **Tags**: #android #mvp #explore

## Context

MVP must implement prefix/range blocking via `CallScreeningService` + `ROLE_CALL_SCREENING` (API 29+), ship as OSS (F-Droid-friendly), and stay simple.

## Decision

1. **Kotlin + Jetpack** native app (not Flutter/RN).
2. **`minSdk` = 29** (Android 10).
3. **Jetpack Compose** for MVP UI.

## Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Flutter/RN + native screening plugin | Extra runtime and bridge for the critical path; weaker F-Droid story |
| minSdk &lt; 29 | No third-party call screening role without being default dialer |
| XML Views | More boilerplate; Compose is the current Jetpack default |

## Consequences

- **Positive**: Direct Telecom integration; smaller complexity for blocking correctness.
- **Negative**: No iOS client path from this stack (already fog / out of near-term destination).
- **Constraint**: All screening logic stays in a native `CallScreeningService` bound under the call-screening role.

## References

- [Decide: Android client stack for MVP](https://github.com/chatondearu/nomorecallcenter/issues/5)
- [Research: Android Call Screening…](https://github.com/chatondearu/nomorecallcenter/issues/2)
- [[ADR-001-mvp-product-constraints|ADR-001]]
