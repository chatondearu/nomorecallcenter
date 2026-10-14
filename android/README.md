# Android client (Slice 5)

Kotlin + Jetpack Compose Call Screening app (ADR-003, minSdk 29).

## Modules

| Module | Role |
| ------ | ---- |
| `:matcher` | Pure JVM rule matcher (prefix/exact/TTL) + unit tests |
| `:app` | Compose UI, Better Auth login, sync, `CallScreeningService`, stats |

## Prerequisites

- JDK 17+
- Android SDK (`ANDROID_HOME`) with platform 35 for `assembleDebug`
- Self-hosted API reachable from the device/emulator (`doc/api.md`)

## Verify (no Android SDK required)

```bash
cd android
export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || echo /opt/homebrew/opt/openjdk@17)"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :matcher:test
```

## Assemble debug APK (needs SDK)

```bash
cd android
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

If `ANDROID_HOME` is unset or incomplete, Gradle will fail on the `:app` module — that is expected. Matcher JVM tests still run.

## Manual path

1. Point the app at your instance base URL (e.g. `http://10.0.2.2:3000` on emulator).
2. Sign in with email/password (Better Auth session cookie).
3. Grant **Call Screening** role — Home does **not** claim “protected” until the role is held (ADR-008).
4. Sync lists + personal rules; create a personal TTL rule; optionally propose to global.
5. Stats: personal counters; optional instance aggregates.

Silent reject is the default when a shared-list prefix or active personal rule matches.
