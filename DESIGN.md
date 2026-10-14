# DESIGN.md — nomorecallcenter

Agent-readable design system. **Source of truth for UI work.**  
Stitch export under `design/stitch-reference/` is **inspiration only** — do not copy HTML page-by-page (components drift between screens).

## Product & brand

- **Name**: nomorecallcenter  
- **Essence**: calm, utilitarian, privacy-first — « Un téléphone silencieux, un contrôle limpide »  
- **Language**: French (FR-first)  
- **Visual system**: Material Design 3 (tonal surfaces, role colors, no neon / no purple-gradient SaaS cliché)

### Correct product framing (do not copy Stitch myths)

Stitch mocks sometimes invent offline-only / P2P / SIP / “cluster” stories. **Ignore those.** Canonical product (ADR-001…008):

- Android Call Screening + **self-hosted multi-user** server (invite-only, Better Auth)
- Shared lists moderated; personal TTL rules synced
- Privacy: **prefix / hash**, not clear full numbers in stats
- Default block: **silent reject**

## Surfaces

| Surface | Stack | Notes |
| ------- | ----- | ----- |
| Android app | Compose Material 3 | Bottom nav: Accueil · Règles · Stats · Paramètres |
| Public site | Nuxt + shared layer | Landing + docs links; F-Droid / self-host CTAs |
| Admin | Nuxt admin + shared layer | Sidebar; proposals; invites; live event feed |

Same **MD3 role names** and hex values on all three (Android via generated `ColorScheme`).

## Seed & colors

- **Seed**: `#1A6352` (Deep Forest Pine)  
- **Scheme**: MD3 tonal spot (generate full light/dark via `@material/material-color-utilities` in the Nuxt shared layer — BAJO-style)  
- **Token file**: `design/tokens/md3-colors.json` (checked-in light snapshot from Stitch; dark regenerated from seed)

### Semantic extras (not raw hex in components)

| Role | Meaning |
| ---- | ------- |
| primary / primary-container | Protection active, primary CTAs |
| secondary | Framing, secondary chips |
| tertiary | Technical metadata, sync/instance hints |
| error | Destructive / blocked emphasis (use sparingly) |
| status-blocked | Muted rose for “rejeté” (prefer token over raw `#8E4A49`) |
| status-allowed | Tempered jade for “légitime” |
| status-watch | Ochre for heuristic / pending |

Never hardcode random hex in Vue/Kotlin UI — only MD3 / semantic tokens.

## Typography

| Surface | Family |
| ------- | ------ |
| Android | **Roboto** / Compose Material3 defaults |
| Web (site + admin) | **Roboto Flex** (or Roboto) — **not Inter** (Stitch default discarded) |

Use MD3 type roles: `display` · `headline` · `title` · `body` · `label` (see Stitch scale in `design/stitch-reference/` for sizes; remap family only).

Phone numbers / prefixes / hashes: tabular nums (`tnum`) where available.

## Layout & shape

- 8dp grid (4dp half-step)  
- Radii: 4 / 8 / 12 / 16 / full (chips, FABs, nav pills)  
- Elevation: **tonal surfaces**, not heavy shadows  
- Touch targets ≥ 48dp on mobile  

## Components (Reka first on web)

Build in Nuxt **shared layer** with **UnoCSS** shortcuts/aliases + **Reka UI** primitives. Custom components only if Reka cannot cover the pattern.

Priority patterns:

1. Status / shield card (protection on/off)  
2. Stat tiles (counts)  
3. Activity / event rows (prefix or hash, action, time)  
4. TTL chips (`1h` `24h` `7j` `30j` `Permanent`)  
5. FAB / primary filled button  
6. Bottom nav (app) / sidebar (admin)  
7. Proposal queue row (approve / reject)  
8. Outlined fields (instance URL, prefix input)

Histoire documents these in the shared package (isolated from consumer `nuxt.config`).

## Styling rules (agents)

1. **UnoCSS only** for styling on web — no new scattered `.css` files (exception: Histoire setup / reset, documented).  
2. Prefer **theme aliases** and shortcuts (`bg-primary`, `text-on-surface`, `rounded-md`, …).  
3. **Reka UI** before custom widgets.  
4. **MD3 roles only** — no one-off brand hex in templates.  
5. Copy in **French**; keep terminology aligned with PRD (proposition, liste globale, rejet silencieux).  
6. Do **not** resurrect Stitch copy about “100% hors ligne”, SQLite-as-product, federation, or open signup.

## Reference screens (structure, not pixel-perfect)

Under `design/stitch-reference/`:

| Folder | Use for |
| ------ | ------- |
| `…_accueil_statut` | App home / protection card / bottom nav |
| `…_r_gles_de_blocage` | Rules list |
| `…_ajouter_une_r_gle` | Add rule + TTL |
| `…_statistiques_historique` | Stats |
| `…_site_public_documentation` | Marketing IA (hero, 3 steps, self-host) |
| `…_admin_vue_d_ensemble_*` | Admin overview density |
| `…_admin_file_de_mod_*` | Proposals queue |

Missing vs PRD (still to design in-product): onboarding Call Screening role, settings/instance URL, invites UI, admin live feed page.

## Related

- ADR-009 (design system)  
- PRD-1 · PLAN-1  
- Explore ticket: Design system MD3 web + Android alignment  
