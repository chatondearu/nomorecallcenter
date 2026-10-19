#!/usr/bin/env bash
# PNG screenshots per design/android-routes.json (emulator or USB device).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

CONFIG="${ANDROID_DESIGN_ROUTES:-design/android-routes.json}"
INSTALL="${ANDROID_DESIGN_INSTALL:-1}"

if [[ ! -f "$CONFIG" ]]; then
  echo "[android:capture] missing $CONFIG" >&2
  exit 1
fi

if ! command -v adb >/dev/null 2>&1; then
  echo "[android:capture] adb not found" >&2
  exit 1
fi

if ! adb devices | awk 'NR>1 && $2=="device" { found=1 } END { exit !found }'; then
  echo "[android:capture] no device — run: pnpm android:emulator (or connect USB / wireless adb)" >&2
  exit 1
fi

PKG="$(node -e "console.log(JSON.parse(require('fs').readFileSync('$CONFIG','utf8')).package)")"
ACTIVITY="$(node -e "console.log(JSON.parse(require('fs').readFileSync('$CONFIG','utf8')).activity)")"
EXTRA="$(node -e "console.log(JSON.parse(require('fs').readFileSync('$CONFIG','utf8')).intentExtra)")"
OUT_DIR="$(node -e "console.log(JSON.parse(require('fs').readFileSync('$CONFIG','utf8')).outDir)")"
SETTLE_MS="$(node -e "console.log(JSON.parse(require('fs').readFileSync('$CONFIG','utf8')).settleMs || 2500)")"
mkdir -p "$OUT_DIR"

if [[ "$INSTALL" == "1" ]]; then
  echo "[android:capture] installDebug"
  (cd android && ./gradlew :app:installDebug)
fi

COMPONENT="${PKG}/${ACTIVITY}"
ROUTES_JSON="$(node -e "console.log(JSON.stringify(JSON.parse(require('fs').readFileSync('$CONFIG','utf8')).routes))")"

node --input-type=module -e "
const routes = JSON.parse(process.argv[1]);
for (const { id, route } of routes) {
  console.log(id + '\t' + route);
}
" "$ROUTES_JSON" | while IFS=$'\t' read -r id route; do
  echo "[android:capture] ${id} (${route})"
  adb shell am start -W -n "$COMPONENT" \
    --es "$EXTRA" "$route" \
    -a android.intent.action.MAIN \
    -c android.intent.category.LAUNCHER >/dev/null
  sleep "$(node -e "console.log(Number(process.argv[1])/1000)" "$SETTLE_MS")"
  adb exec-out screencap -p > "${OUT_DIR}/${id}.png"
done

echo "[android:capture] done → ${OUT_DIR}/"
