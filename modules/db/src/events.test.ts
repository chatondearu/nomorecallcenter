import { describe, expect, it } from 'vitest'
import {
  eventsRetentionDays,
  hashCallerNumber,
  isEventAction,
  normalizeCallerNumber,
  normalizeEventHash,
  normalizeEventPrefix,
  retentionCutoff,
} from './events.js'

describe('normalizeEventPrefix', () => {
  it('accepts short digit prefixes', () => {
    expect(normalizeEventPrefix('0892')).toBe('0892')
    expect(normalizeEventPrefix('+33 6 12')).toBe('33612')
  })

  it('rejects clear full numbers (>8 digits)', () => {
    expect(normalizeEventPrefix('0612345678')).toBeNull()
    expect(normalizeEventPrefix('33612345678')).toBeNull()
  })
})

describe('normalizeCallerNumber', () => {
  it('canonicalizes FR national and international forms', () => {
    expect(normalizeCallerNumber('0612345678')).toBe('33612345678')
    expect(normalizeCallerNumber('+33 6 12 34 56 78')).toBe('33612345678')
    expect(normalizeCallerNumber('1')).toBeNull()
  })

  it('hashes the normalized digits', () => {
    const hash = hashCallerNumber('33612345678')
    expect(hash).toHaveLength(64)
    expect(hash).toBe(hashCallerNumber('33612345678'))
  })
})

describe('normalizeEventHash', () => {
  it('accepts 64-char hex', () => {
    const h = 'a'.repeat(64)
    expect(normalizeEventHash(h)).toBe(h)
  })

  it('rejects short or non-hex', () => {
    expect(normalizeEventHash('abc')).toBeNull()
    expect(normalizeEventHash(`${'g'.repeat(64)}`)).toBeNull()
  })
})

describe('isEventAction', () => {
  it('accepts known actions', () => {
    expect(isEventAction('blocked')).toBe(true)
    expect(isEventAction('nope')).toBe(false)
  })
})

describe('retention', () => {
  it('defaults to 90 days', () => {
    expect(eventsRetentionDays(undefined)).toBe(90)
    expect(eventsRetentionDays('0')).toBe(90)
  })

  it('computes cutoff', () => {
    const now = new Date('2026-10-14T12:00:00.000Z')
    const cut = retentionCutoff(now, 7)
    expect(cut.toISOString()).toBe('2026-10-07T12:00:00.000Z')
  })
})
