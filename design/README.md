# Design loop (web)

Visual review targets for [`cda-design-loop`](../../AGENTS.md) and Playwright captures.

| File | Role |
| ---- | ---- |
| [routes.json](routes.json) | Site + admin paths and base URL env keys |
| [../DESIGN.md](../DESIGN.md) | Brief (MD3, typography, anti-slop) |
| [../design/tokens/](../design/tokens/) | Color tokens |

**Not in scope (yet):** Android Compose — see [android-emulator.md](android-emulator.md).

```bash
pnpm install && npx playwright install chromium   # once (inside nix develop on Linux)
pnpm dev:design          # Postgres + Nuxt API + admin (fresh UI, no compose API on :3000)
pnpm auth:acceptance     # once — bootstrap admin (or use existing DB)
pnpm design:auth         # saves .playwright/admin.json
pnpm design:capture      # PNGs under design/shots/ (375 / 768 / 1440)
```

Override bases with `DEV_URL_SITE` and `DEV_URL_ADMIN` in `.env`.
