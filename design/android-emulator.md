# Android visual review (emulator)

Compose UI is outside `cda-design-loop` (web). Use **ADB screencaps** on an emulator or USB device.

## Prerequisites

- `nix develop` (JDK, Android SDK, **emulator** + `adb` in `flake.nix`)
- KVM / nested virt on Linux (emulator x86_64)
- First run downloads a system image (~1 GB) via `sdkmanager`

## Quick path

```bash
# Terminal 1 — boot emulator (or use Android Studio AVD)
pnpm android:emulator:daemon

# API for login tests from emulator (optional)
# .env: BETTER_AUTH_URL=http://10.0.2.2:3000 && docker compose up -d --force-recreate api

# Terminal 2 — install debug APK + PNGs
pnpm android:design:capture
# → design/shots-android/{home,rules,stats,settings,login}.png
```

`android:design:capture` runs `:app:installDebug` by default. Skip with `ANDROID_DESIGN_INSTALL=0`.

## How navigation works

Debug builds accept an intent extra (no deep links in the manifest):

```bash
adb shell am start -n fr.chatondearu.nmcc/.MainActivity --es NMCC_DESIGN_ROUTE rules
```

Routes are listed in [`android-routes.json`](android-routes.json). Same constant in `MainActivity.EXTRA_DESIGN_ROUTE`.

## Environment

| Variable | Default | Role |
| -------- | ------- | ---- |
| `ANDROID_AVD_NAME` | `nmcc_design` | AVD to create/start |
| `ANDROID_SYSTEM_IMAGE` | `system-images;android-34;google_apis;x86_64` | `sdkmanager` package |
| `ANDROID_EMULATOR_HEADLESS` | `0` | Set `1` for `-no-window` |
| `ANDROID_EMULATOR_DAEMON` | (set by `android:emulator:daemon`) | Exit after boot, leave emulator running |
| `ANDROID_DESIGN_INSTALL` | `1` | Run `installDebug` before capture |

## Instance URL on emulator

- App **Instance URL**: `http://10.0.2.2:3000` (host loopback from the emulator).
- When testing Better Auth login against compose, set `BETTER_AUTH_URL=http://10.0.2.2:3000` in `.env` and recreate the `api` container.

## Critique

Compare PNGs to [`DESIGN.md`](../DESIGN.md) (MD3 roles, French copy). Call Screening role and live call UI still need manual checks.

## Wireless device

Same capture script with `adb connect <ip>:5555` after USB pairing — no emulator required.
