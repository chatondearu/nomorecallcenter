# Android client

Kotlin + Jetpack Compose Call Screening app (ADR-003, minSdk 29). Distribution:
**GitHub Releases** (APK/AAB sideload) + **F-Droid**-oriented builds — Play Store
is out of MVP (ADR-008).

## Modules

| Module | Role |
| ------ | ---- |
| `:matcher` | Pure JVM rule matcher (prefix/exact/TTL) + unit tests |
| `:app` | Compose UI, Better Auth login, sync, `CallScreeningService`, stats |

## Prerequisites

- JDK 17+
- Android SDK (`ANDROID_HOME`) with platform 35 for assemble
- Optional: self-hosted API for sync/stats (`doc/api.md`) — **not required** for call/SMS filtering (offline bundled FR list + local rules)

## Verify (no Android SDK required)

```bash
cd android
export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || echo /opt/homebrew/opt/openjdk@17)"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :matcher:test
```

## Debug APK

```bash
cd android
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17}"
export ANDROID_HOME="${ANDROID_HOME:-/opt/homebrew/share/android-commandlinetools}"
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
# Copy also landed at ../dist/nmcc-debug.apk when built for local validation
```

Offline validation: open the app → enable Call Screening role → optionally allow SMS → add a personal rule or rely on bundled `0892` / `33892` prefixes. No login required.

## GitHub Release (APK / AAB)

Unsigned local artifacts for a Release draft (sign before public store / wide sideload):

```bash
cd android
./gradlew :app:assembleRelease :app:bundleRelease
# APK: app/build/outputs/apk/release/app-release-unsigned.apk
# AAB: app/build/outputs/bundle/release/app-release.aab
```

Suggested Release checklist:

1. Tag `vX.Y.Z`; attach the signed APK (and optional AAB).
2. Notes: instance URL is user-configured; grant **Call Screening** after install.
3. Point operators at root README self-host smoke + this file.

Signing: use your own keystore (`signingConfigs` in `app/build.gradle.kts`) or
`apksigner` / Play App Signing later — not required to build unsigned release
artifacts for CI smoke.

## F-Droid-oriented notes

MVP dependencies are AndroidX / OkHttp / Kotlin only — **no** Play Services,
Firebase, or proprietary Google Maven-only SDKs in `app/build.gradle.kts`.

For an F-Droid recipe (own repo or upstream submission):

- Build: `cd android && ./gradlew :app:assembleRelease`
- `applicationId`: `fr.chatondearu.nmcc`
- Min SDK 29; target/compile SDK 35
- AntiFeatures to expect: needs a reachable self-hosted API (Network)
- Do not add Play Billing, SafetyNet/Play Integrity, or GMS-only push for MVP

Official F-Droid inclusion vs a self-hosted fdroidrepo is an ops choice (PRD open
question) — not a product gate.

## Manual path (device)

1. Point the app at your instance base URL (emulator → `http://10.0.2.2:3000`).
2. Sign in with email/password (Better Auth session cookie).
3. Grant **Call Screening** role — Home does **not** claim “protected” until the role is held (ADR-008).
4. Sync lists + personal rules; create a personal TTL rule; optionally propose to global.
5. Stats: personal counters; optional instance aggregates.

Silent reject is the default when a shared-list prefix or active personal rule matches.

Device acceptance (physical API 29+ silent reject of a seeded prefix) remains a
manual check before calling MVP “shipped” in the PRD.
