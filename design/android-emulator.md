# Android visual review (planned)

`cda-design-loop` targets **rendered web** only. Kotlin Compose screens need a
separate eyes channel — typically an **Android emulator** on the dev machine.

## Target workflow (not implemented yet)

1. **Emulator** — API 29+ system image in the Nix/Android SDK workflow (or Android Studio AVD).
2. **Install** — `cd android && ./gradlew :app:installDebug` with `adb devices` showing the emulator.
3. **Capture** — `adb exec-out screencap -p > design/shots-android/<screen>.png` after navigating key flows (home, login, settings, Call Screening role).
4. **Critique** — same `DESIGN.md` / MD3 role tokens as web; OEM quirks noted manually.

## Instance URL on emulator

- API on host: `http://10.0.2.2:3000` (not LAN IP).
- Set `BETTER_AUTH_URL=http://10.0.2.2:3000` in `.env` and recreate the API container when testing login from the emulator against compose.

## Out of scope for first pass

- Playwright (no WebView for main UI).
- Instrumented screenshot tests in CI (optional later).

Track progress on [PLAN-2](../plans/PLAN-2-local-dev-and-device-acceptance.md).
