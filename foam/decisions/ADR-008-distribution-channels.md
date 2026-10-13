# ADR-008 — Distribution channels for MVP

- **Status**: accepted
- **Date**: 2026-10-13
- **Tags**: #distribution #android #mvp #explore

## Context

OSS Android app (AGPL) using `ROLE_CALL_SCREENING` (ADR-003) plus self-host backend (ADR-004). Need MVP ship channels without Play policy blocking the critical path.

## Decision

1. **Android MVP**: **GitHub Releases** (APK/AAB sideload) + **F-Droid** (direct or submission pipeline). **Play Store out of MVP** (vision later).
2. **Backend/admin MVP**: **OCI images** (e.g. GHCR) + documented **`docker compose`** (BAJO-style).
3. **Onboarding**: mandatory Call Screening role request; app must not claim “protected” until `ROLE_CALL_SCREENING` is held.

## Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Play Store in MVP | Review / sensitive-permission friction for screening apps |
| Cloud-only installers | Contradicts self-host OSS story |

## Consequences

- **Positive**: Matches OSS audience; sidestep Play gate for v1.
- **Negative**: Smaller reach; users must enable unknown sources / F-Droid.
- **Constraint**: Document role UX for sideload and F-Droid builds.

## References

- [Decide: Distribution channels for MVP](https://github.com/chatondearu/nomorecallcenter/issues/10)
- [[ADR-003-android-client-stack|ADR-003]], [[ADR-004-backend-stack|ADR-004]]
