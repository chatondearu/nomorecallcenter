/** ADR-006 call-event actions */
export const EVENT_ACTIONS = ['screened', 'blocked', 'allowed'] as const
export type EventAction = typeof EVENT_ACTIONS[number]

/** Prefix length bound: long enough for FR ranges, short enough to reject clear full numbers. */
export const EVENT_PREFIX_MIN = 2
export const EVENT_PREFIX_MAX = 8

/** SHA-256 hex length */
export const EVENT_HASH_LENGTH = 64

const HASH_RE = /^[a-f0-9]{64}$/i

export function isEventAction(value: unknown): value is EventAction {
  return typeof value === 'string' && (EVENT_ACTIONS as readonly string[]).includes(value)
}

/** Digits-only prefix 2–8 chars (rejects clear full national numbers). */
export function normalizeEventPrefix(raw: unknown): string | null {
  if (typeof raw !== 'string')
    return null
  const digits = raw.replace(/\D/g, '')
  if (digits.length < EVENT_PREFIX_MIN || digits.length > EVENT_PREFIX_MAX)
    return null
  return digits
}

/** Lowercase SHA-256 hex, or null if invalid. */
export function normalizeEventHash(raw: unknown): string | null {
  if (typeof raw !== 'string')
    return null
  const trimmed = raw.trim().toLowerCase()
  if (!HASH_RE.test(trimmed))
    return null
  return trimmed
}

/** Default retention days when EVENTS_RETENTION_DAYS is unset/invalid. */
export const DEFAULT_EVENTS_RETENTION_DAYS = 90

export function eventsRetentionDays(envValue: string | undefined = process.env.EVENTS_RETENTION_DAYS): number {
  const n = Number(envValue)
  if (!Number.isFinite(n) || n < 1)
    return DEFAULT_EVENTS_RETENTION_DAYS
  return Math.floor(n)
}

export function retentionCutoff(now: Date = new Date(), days: number = eventsRetentionDays()): Date {
  return new Date(now.getTime() - days * 86_400_000)
}
