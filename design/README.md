# Design assets

| Path | Role |
| ---- | ---- |
| [`../DESIGN.md`](../DESIGN.md) | **Canonical** agent/human design brief |
| [`tokens/md3-colors.json`](tokens/md3-colors.json) | Checked-in MD3 light tokens + seed |
| [`stitch-reference/`](stitch-reference/) | Raw Stitch export (screens + HTML) — **reference only** |

## Rules of engagement

1. Implement from `DESIGN.md` + tokens + Histoire/shared components — not by forking Stitch `code.html`.
2. Stitch pages **drift** (nav, density, copy). Treat screenshots as IA / hierarchy hints.
3. Strip Stitch product myths (offline-only SQLite, federation, open community signup) — product ADRs win.
4. When the Nuxt shared layer exists, generate dark scheme + Android `Color.kt` from the same seed pipeline.
