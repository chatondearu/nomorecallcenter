#!/usr/bin/env bash
# Start the design AVD if no emulator is connected (requires a system image once).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

AVD_NAME="${ANDROID_AVD_NAME:-nmcc_design}"
SYSTEM_IMAGE="${ANDROID_SYSTEM_IMAGE:-system-images;android-34;google_apis;x86_64}"
EMULATOR_GPU="${ANDROID_EMULATOR_GPU:-swiftshader_indirect}"
HEADLESS="${ANDROID_EMULATOR_HEADLESS:-0}"

if ! command -v adb >/dev/null 2>&1; then
  echo "[android:emulator] adb not found — use nix develop" >&2
  exit 1
fi

if adb devices | awk 'NR>1 && $2=="device" { found=1 } END { exit !found }'; then
  echo "[android:emulator] device already connected"
  adb devices -l
  exit 0
fi

if [[ -z "${ANDROID_HOME:-}" ]]; then
  echo "[android:emulator] ANDROID_HOME unset" >&2
  exit 1
fi

SDKMANAGER="${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager"
AVDMANAGER="${ANDROID_HOME}/cmdline-tools/latest/bin/avdmanager"
EMULATOR="${ANDROID_HOME}/emulator/emulator"

if [[ ! -x "$EMULATOR" ]]; then
  echo "[android:emulator] emulator binary missing — enable includeEmulator in flake.nix or use Android Studio AVD" >&2
  exit 1
fi

if [[ -x "$SDKMANAGER" ]] && ! "$SDKMANAGER" --list_installed 2>/dev/null | grep -qF "$SYSTEM_IMAGE"; then
  echo "[android:emulator] installing $SYSTEM_IMAGE (once, large download)"
  yes | "$SDKMANAGER" "$SYSTEM_IMAGE" >/dev/null
fi

if [[ -x "$AVDMANAGER" ]] && ! "$AVDMANAGER" list avd 2>/dev/null | grep -q "Name: ${AVD_NAME}"; then
  echo "[android:emulator] creating AVD ${AVD_NAME}"
  echo no | "$AVDMANAGER" create avd -n "$AVD_NAME" -k "$SYSTEM_IMAGE" -d pixel_6 --force
fi

EMU_ARGS=(-avd "$AVD_NAME" -no-snapshot-save -no-audio -gpu "$EMULATOR_GPU")
if [[ "$HEADLESS" == "1" ]]; then
  EMU_ARGS+=(-no-window)
fi

echo "[android:emulator] starting ${AVD_NAME}"
"$EMULATOR" "${EMU_ARGS[@]}" &
EMU_PID=$!

cleanup() {
  kill "$EMU_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

adb wait-for-device
echo "[android:emulator] waiting for boot"
for _ in $(seq 1 120); do
  booted="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
  if [[ "$booted" == "1" ]]; then
    echo "[android:emulator] ready"
    adb devices -l
    trap - EXIT
    if [[ "${ANDROID_EMULATOR_DAEMON:-0}" == "1" ]]; then
      echo "[android:emulator] daemon mode (pid $EMU_PID)"
      exit 0
    fi
    wait "$EMU_PID"
    exit 0
  fi
  sleep 2
done

echo "[android:emulator] boot timeout" >&2
exit 1
