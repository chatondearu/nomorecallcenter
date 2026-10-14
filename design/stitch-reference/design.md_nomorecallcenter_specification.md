# DESIGN.md — nomorecallcenter

## 1. Product & Brand Identity
- **Name**: `nomorecallcenter`
- **Mission**: Open-source, self-hosted call screening & call-center spam prevention on Android with privacy-by-design (prefix & cryptographic hash matching, never full numbers).
- **Brand Essence**: Utilitarian, calm, trustworthy, uncompromising privacy ("Un téléphone silencieux, un contrôle limpide").
- **Language**: French (FR-first) with clean native Android / MD3 phrasing.

---

## 2. Material Design 3 Seed & Color Palette

### Seed Hex
- **Primary Seed**: `#1A6352` (Deep Forest Pine / Emerald Calm — conveys quiet authority, privacy, non-commercial security)
- **Neutral Tone**: Warm slate `#5C5F5E`
- **Scheme Choice**: MD3 Tonal Spot / Expressive Utilitarian

### MD3 Token Mappings (Light & Dark)

| Role | Light Hex | Dark Hex | Usage |
|---|---|---|---|
| `md.sys.color.primary` | `#1A6352` | `#82D5BC` | Dominant brand action, active toggles, primary CTA |
| `md.sys.color.on-primary` | `#FFFFFF` | `#00382C` | Content on primary containers/buttons |
| `md.sys.color.primary-container` | `#9EF2D8` | `#005140` | Tonal hero cards, active chips, badges |
| `md.sys.color.on-primary-container` | `#002118` | `#9EF2D8` | Text & icons on primary container |
| `md.sys.color.surface` | `#F8FAF8` | `#111413` | App/page base background |
| `md.sys.color.surface-container-lowest` | `#FFFFFF` | `#0C0F0E` | High-contrast cards, input backgrounds |
| `md.sys.color.surface-container` | `#ECEFEF` | `#1E2120` | Default card backgrounds, list items |
| `md.sys.color.surface-container-high` | `#E6E9E8` | `#282B2A` | Elevated cards, bottom sheets |
| `md.sys.color.on-surface` | `#191C1B` | `#E1E3E2` | Primary text and headings |
| `md.sys.color.on-surface-variant` | `#3F4945` | `#BFC9C4` | Secondary text, captions, metadata |
| `md.sys.color.outline` | `#6F7975` | `#89938E` | Subtle borders, dividers, chip outlines |
| `md.sys.color.outline-variant` | `#BEC9C4` | `#3F4945` | Decorative borders, non-critical lines |
| `md.sys.color.error` | `#BA1A1A` | `#FFB4AB` | Blocked alerts, destructive delete actions |
| `md.sys.color.error-container` | `#FFDAD6` | `#93000A` | Rejection banner tone |

---

## 3. Typography Scale (Inter / Roboto Native)

- **Display Large**: 57px / Line height 64px (Marketing Hero)
- **Headline Medium**: 28px / Line height 36px (Android Section & Admin Main)
- **Title Large**: 22px / Line height 28px (Top App Bars, Card Headers)
- **Title Medium**: 16px / Line height 24px (List Item Titles, Rule Prefixes)
- **Body Large**: 16px / Line height 24px (Standard text, readouts)
- **Body Medium**: 14px / Line height 20px (Metadata, status indicators, secondary text)
- **Label Large**: 14px / Line height 20px (Button labels, interactive action targets)
- **Label Small**: 11px / Line height 16px (TTL badges, hash chips, tags)

---

## 4. Spacing & Density
- Base grid: **4dp / 8dp system**
- Margins: 16dp (Mobile edge padding), 24dp (Tablet / Admin container), 32dp+ (Web landing)
- Border radii:
  - Small (4dp): Badges, technical hash pills
  - Medium (8dp): Buttons, input fields
  - Large (16dp): Cards, list card items
  - Extra Large (28dp): Bottom sheets, dialogs, floating status pill
- Elevation: MD3 tonal elevation (no harsh drop shadows, tonal overlay tinted with Primary).

---

## 5. Standard Component Patterns

### Chips (TTL & Status)
- Presets: `1h` · `24h` · `7j` · `30j` · `Permanent`
- Active: Background `primaryContainer`, border none, text `onPrimaryContainer`.
- Inactive: Background transparent, border 1dp `outlineVariant`, text `onSurfaceVariant`.

### Call-Screening Status Indicator
- Big tactile status card:
  - Shield / Soundless Icon + status pill ("Protection active", "Rejet silencieux").
  - Clear toggle switch with tactile visual feedback.

### Privacy Rules
- Never render raw numbers in telemetry, stats, or logs.
- Format: `+33 1 62 ** **` (Prefix) or `SHA-256 (e8b4…31a9)` with tap-to-verify.
