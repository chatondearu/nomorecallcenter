---
name: Sérénité Téléphonique
colors:
  surface: '#f7faf6'
  surface-dim: '#d8dbd7'
  surface-bright: '#f7faf6'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f1f4f0'
  surface-container: '#ecefeb'
  surface-container-high: '#e6e9e5'
  surface-container-highest: '#e0e3df'
  on-surface: '#181c1a'
  on-surface-variant: '#3f4945'
  inverse-surface: '#2d312f'
  inverse-on-surface: '#eef2ed'
  outline: '#6f7975'
  outline-variant: '#bfc9c4'
  surface-tint: '#226958'
  primary: '#004a3c'
  on-primary: '#ffffff'
  primary-container: '#1a6352'
  on-primary-container: '#97dcc7'
  inverse-primary: '#8fd4bf'
  secondary: '#4d635b'
  on-secondary: '#ffffff'
  secondary-container: '#cde5db'
  on-secondary-container: '#51675f'
  tertiary: '#1f4556'
  on-tertiary: '#ffffff'
  tertiary-container: '#385d6e'
  on-tertiary-container: '#aed5e8'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#abf0da'
  primary-fixed-dim: '#8fd4bf'
  on-primary-fixed: '#002019'
  on-primary-fixed-variant: '#005141'
  secondary-fixed: '#d0e8de'
  secondary-fixed-dim: '#b4ccc2'
  on-secondary-fixed: '#0a1f19'
  on-secondary-fixed-variant: '#364b44'
  tertiary-fixed: '#c2e8fc'
  tertiary-fixed-dim: '#a6cce0'
  on-tertiary-fixed: '#001f2a'
  on-tertiary-fixed-variant: '#254b5c'
  background: '#f7faf6'
  on-background: '#181c1a'
  surface-variant: '#e0e3df'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 57px
    fontWeight: '400'
    lineHeight: 64px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '400'
    lineHeight: 40px
    letterSpacing: 0px
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '500'
    lineHeight: 36px
    letterSpacing: 0px
  headline-md:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '400'
    lineHeight: 36px
    letterSpacing: 0px
  headline-sm:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '400'
    lineHeight: 32px
    letterSpacing: 0px
  title-lg:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '400'
    lineHeight: 28px
    letterSpacing: 0px
  title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0.15px
  title-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.4px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-tablet: 1.5rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-tablet: 2rem
  margin-desktop: 3rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system establishes a quiet, unyielding digital shield for personal communications. The core philosophy centers on restored tranquility, technical transparency, and sovereign privacy. Built specifically for an open-source, self-hosted call prevention platform, the visual posture avoids alarmist or aggressive security tropes (such as harsh warning reds, shield badges, or synthetic neon accents) in favor of a poised, institutional, and utilitarian calm.

The visual style is rooted in authentic Material Design 3 guidelines, adapted into a minimalist, privacy-first interface. It prioritizes legibility, immediate clarity of incoming caller verification states, and cognitive relief from unsolicited communications.

Key Brand Pillars:
- **Calm Sovereignty:** The phone is once again a private, peaceful instrument. The UI reflects zero urgency unless a call is legitimate and critical.
- **Utilitarian Transparency:** Technical rule-matching (regex, decentralized blocklists, local SQLite heuristics, SIP rules) is exposed cleanly with absolute honesty, using sober data displays rather than gamified scores.
- **Privacy-by-Architecture:** Visual cues constantly reinforce local execution—no telemetry indicators, zero cloud-dependence markers, and explicit on-device state badges.

## Colors

The palette derives from a deep forest pine seed (`#1A6352`), producing a grounding botanical slate tonal range. It balances cold technical utility with organic reassurance, generating full tonal palettes across M3 roles.

### Functional Palette Logic
- **Primary (`#1A6352` / M3 Pine):** Signifies authorized interactions, active protection, verified caller identities, and primary navigational intents.
- **Secondary (`#4D635B` / M3 Slate Sage):** Manages secondary filtering mechanics, rule categorization tags, and ambient UI framing.
- **Tertiary (`#3E6374` / M3 Steel Petrol):** Dedicated to technical metadata, self-hosted sync states, local database versions, and regex engine statuses.
- **Neutral & Surface Tiers:** Tinted surfaces ensure seamless dark and light theme transitions, providing depth without reliance on stark achromatic gray.
- **Status & Defensive Roles:**
  - *Filtré / Rejeté (Defensive block):* `#8E4A49` (Muted Tuscan Rose, avoiding aggressive primary red to prevent unnecessary user agitation).
  - *Légitime / Vérifié (Safe):* `#2A6A4E` (Tempered Jade).
  - *Vigilance / Heuristique (Suspicious):* `#7D5721` (Warm Ochre).

### Light & Dark Modes
Light mode delivers high-contrast outdoor utility for phone use on the move. Dark mode applies M3 tonal surface elevations with deep slate-black tones (`#111413`), minimizing OLED power consumption and screen glare during nocturnal disturbance blocks.

## Typography

The typographic hierarchy relies entirely on `Inter` for its neutral clarity, robust micro-legibility at tabular widths, and superior rendering of technical phone metadata, timestamps, and international E.164 dialing standards.

### Localization & Tone
Copy defaults to succinct, poised French. Terminology remains rigorous, avoiding technical jargon where possible while maintaining absolute precision:
- "Appels indésirables interceptés" (Spam calls intercepted)
- "Règle de filtrage local active" (Local filtering rule active)
- "Journal confidentiel sur l'appareil" (Confidential on-device log)
- "Aucune fuite de données réseau" (Zero network data leakage)

### Numerical and Code Display
Phone numbers, prefix matching patterns (`+33 1 62...`), and regex identifiers utilize tabular numbers (`tnum` OpenType feature) to ensure horizontal visual stability across lists and call logs.

## Layout & Spacing

The layout system strictly follows an 8dp baseline grid (with a 4dp half-step for micro-adjustments like inline badge padding and icon-to-label gaps). 

### Layout Model
- **Mobile (Compact, < 600dp):** Single-column fluid view. Standard 16dp (`margin`) canvas margins with 16dp (`gutter`) gutters between dynamic listing tiles. The screen prioritizes one-handed ergonomics: high-priority control zones and filtering toggles sit within bottom reach, with top-level statistics settled above the natural fold.
- **Foldable & Tablet (Medium, 600dp – 839dp):** Dual-pane structure. The left pane holds the call log and active screening queues (360dp width); the right pane hosts deep inspect panels (filter rule editor, caller heuristic breakdown).
- **Desktop / Self-Hosted Web Admin (Expanded, ≥ 840dp):** Fluid container constrained to 1200dp max-width, 12-column grid, 24dp gutters, and 48dp margins.

### Vertical Rhythm
Every interactive target conforms to the minimum 48x48dp touch footprint, maintaining generous breathing space (`space-md`) between list items to prevent mis-taps on screening actions.

## Elevation & Depth

This design system avoids heavy drop shadows and faux light physics. In strict alignment with Material Design 3, spatial depth is communicated through **Tonal Surface Layers** tinted with the primary hue (`#1A6352`).

### Surface Elevation Tiers
1. **Surface Container Lowest:** Base canvas layer (`#F8FAF8` in light mode, `#0E1110` in dark mode).
2. **Surface Container Low:** Standard grouped list backgrounds and non-interactive sections.
3. **Surface Container:** Default surface for cards, caller activity history rows, and quiet state modules.
4. **Surface Container High:** Modals, swipe action panels, filter rule expansion drawers, and bottom sheets.
5. **Surface Container Highest:** Tooltips, snackbars, and transient interception notifications.

### Outlines and Separation
Elevation steps are paired with ultra-subtle 1dp hairline outlines in `outline-variant` (low contrast, ~12% opacity) rather than structural borders. Ambient drop shadows are reserved exclusively for floating action buttons or transient dialog scrims, using a soft, diffused 8dp blur tinted with forest slate (`rgba(26, 99, 82, 0.08)`).

## Shapes

The design system employs a `Rounded` (Level 2) shape expression, adhering to Material 3 standard component radiuses while maintaining an orderly, calm appearance.

### Shape Class Allocation
- **Extra Small (4dp / 0.25rem):** Text selection handles, micro tag indicators.
- **Small (8dp / 0.5rem):** Filter input fields, tabular rows, checkbox controls, snackbar containers.
- **Medium (12dp / 0.75rem):** Cards, call log entries, system status summary containers.
- **Large (16dp / 1.0rem):** Navigation drawer panels, dynamic bottom sheets, primary dialogs.
- **Full (Pill / 9999px):** Filter chips, screening toggle buttons, call status badges, and FABs.

Sharp corners are avoided to preserve the approachable and reassuring aesthetic, while exaggerated curves are limited to discrete interactive tokens (chips, pills) to keep the app clean and utilitarian.

## Components

### 1. Buttons (Boutons)
- **Filled Button:** Primary actions (e.g., "Bloquer le préfixe", "Activer la protection"). Background is Primary Forest (`#1A6352`) with white label text. 20dp horizontal padding, 40dp height, 20dp border-radius (pill).
- **Tonal Button:** Secondary supportive actions (e.g., "Modifier la règle"). Surface Container High background with Primary-tinted text.
- **Outlined Button:** Neutral operations (e.g., "Exporter la base locale"). Transparent fill with a 1dp `outline-variant` border.
- **Text Button:** Low-emphasis navigation (e.g., "Ignorer", "Détails").

### 2. Chips (Filtres & Statuts)
- **Assist & Filter Chips:** Pill-shaped (32dp height), housing contextual filters (e.g., "Démarchage présumé", "Numéros mobiles", "Préfixes ARCEP").
- Selected state activates a soft tinted background with a checkmark icon (`space-xs` gap between icon and label).

### 3. Call Log List (Journal de Filtrage)
- Two-line and three-line list items wrapped in `Surface Container Low` cards.
- **Leading Element:** 40dp circular avatar showing an iconified state (shielded, silenced, or verified contact) colored according to classification.
- **Headline:** Standardized phone number or resolved contact name.
- **Supporting Text:** Reason for blocking or passing (e.g., "Règle ARCEP: Démarchage légal interdit", "Liste blanche locale").
- **Trailing Element:** Monospaced timestamp (`body-sm`) plus an overflow menu trigger for rule creation.

### 4. Checkboxes & Switches (Paramètres de Confidentialité)
- **Switches:** Used for persistent toggles ("Filtrage automatique", "Blocage strict des préfixes 0162/0163"). Pill container with smooth glide states, providing high visual clarity between active Pine and disabled Neutral tones.
- **Checkboxes:** Standard 18dp rounded square controls with a solid primary fill when checked, reserved for multi-select batch operations in the log.

### 5. Input Fields (Champs de Recherche & Regex)
- Outlined text fields with label integration in line with M3 specs.
- Clear trailing icons for instant reset of search terms.
- Monospace sub-styling when the input enters "Mode Expression Régulière (Regex)".

### 6. Cards (Cartes Statistiques & État Système)
- **Shield Health Card (État du Bouclier):** Elevated `Surface Container` at top of dashboard. Displays active protection status, last local database sync timestamp, and an explicit "100% hors ligne" badge.
- Flat appearance with subtle tonal fill rather than harsh borders, providing clean visual calm.