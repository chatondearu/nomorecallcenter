#!/usr/bin/env bash
# Capture viewport PNGs via Playwright (cda-design-loop eyes).
set -euo pipefail

SKIP_MSG='SKIP_VISUAL: Playwright not available'

usage() {
  echo "Usage: $(basename "$0") <URL> <OUT_DIR> [storage_state.json]" >&2
  exit 1
}

[[ $# -ge 2 && $# -le 3 ]] || usage
URL="$1"
OUT_DIR="$2"
STORAGE="${3:-}"
mkdir -p "$OUT_DIR"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! command -v node >/dev/null 2>&1; then
  echo "$SKIP_MSG" >&2
  exit 2
fi

if ! node -e "require('playwright')" 2>/dev/null; then
  echo "$SKIP_MSG" >&2
  exit 2
fi

WIDTHS=(375 768 1440)
STORAGE_ARG=""
if [[ -n "$STORAGE" && -f "$STORAGE" ]]; then
  STORAGE_ARG="$(node -e "console.log(require('path').resolve(process.argv[1]))" "$STORAGE")"
fi

for w in "${WIDTHS[@]}"; do
  OUT="$OUT_DIR/w${w}.png"
  node --input-type=module -e "
import { launchBrowser } from './scripts/playwright-browser.mjs';

const url = process.argv[1];
const out = process.argv[2];
const storage = process.argv[3] || '';
const width = Number(process.argv[4]);

const browser = await launchBrowser();
const context = await browser.newContext(
  storage ? { storageState: storage } : {},
);
const page = await context.newPage({ viewport: { width, height: 900 } });
await page.goto(url, { waitUntil: 'networkidle' });
await page.screenshot({ path: out, fullPage: true });
await browser.close();
" "$URL" "$OUT" "$STORAGE_ARG" "$w" || {
    echo "cda-design-shot: capture failed (width ${w})" >&2
    exit 3
  }
done
