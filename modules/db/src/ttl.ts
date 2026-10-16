/** ADR-007 TTL presets */
export const TTL_PRESETS = ['1h', '24h', '7d', '30d', 'permanent'] as const
export type TtlPreset = typeof TTL_PRESETS[number]

export const MATCH_TYPES = ['exact', 'prefix'] as const
export type MatchType = typeof MATCH_TYPES[number]

export const RULE_EFFECTS = ['allow', 'block'] as const
export type RuleEffect = typeof RULE_EFFECTS[number]

const TTL_MS: Record<Exclude<TtlPreset, 'permanent'>, number> = {
  '1h': 3_600_000,
  '24h': 86_400_000,
  '7d': 7 * 86_400_000,
  '30d': 30 * 86_400_000,
}

export function isTtlPreset(value: unknown): value is TtlPreset {
  return typeof value === 'string' && (TTL_PRESETS as readonly string[]).includes(value)
}

export function isMatchType(value: unknown): value is MatchType {
  return typeof value === 'string' && (MATCH_TYPES as readonly string[]).includes(value)
}

export function isRuleEffect(value: unknown): value is RuleEffect {
  return typeof value === 'string' && (RULE_EFFECTS as readonly string[]).includes(value)
}

/** Compute expiresAt from now for a preset; permanent → null. */
export function expiresAtForPreset(preset: TtlPreset, from: Date = new Date()): Date | null {
  if (preset === 'permanent')
    return null
  return new Date(from.getTime() + TTL_MS[preset])
}

/** Active when permanent (null) or expiresAt is still in the future. */
export function isRuleActive(expiresAt: Date | null, now: Date = new Date()): boolean {
  return expiresAt === null || expiresAt.getTime() > now.getTime()
}

/** Digits-only pattern, 2–15 chars (FR national / short prefixes). */
export function normalizePattern(raw: unknown): string | null {
  if (typeof raw !== 'string')
    return null
  const digits = raw.replace(/\D/g, '')
  if (digits.length < 2 || digits.length > 15)
    return null
  return digits
}
