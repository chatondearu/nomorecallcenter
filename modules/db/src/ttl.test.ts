import { describe, expect, it } from 'vitest'
import { expiresAtForPreset, isRuleActive, normalizePattern } from './ttl.js'

describe('ttl presets', () => {
  const from = new Date('2026-10-14T12:00:00.000Z')

  it('computes expiresAt for 7d', () => {
    const expires = expiresAtForPreset('7d', from)
    expect(expires?.toISOString()).toBe('2026-10-21T12:00:00.000Z')
  })

  it('permanent has null expiresAt', () => {
    expect(expiresAtForPreset('permanent', from)).toBeNull()
  })

  it('active when permanent or future', () => {
    expect(isRuleActive(null, from)).toBe(true)
    expect(isRuleActive(new Date('2026-10-15T00:00:00.000Z'), from)).toBe(true)
    expect(isRuleActive(new Date('2026-10-14T11:00:00.000Z'), from)).toBe(false)
  })

  it('normalizes digit patterns', () => {
    expect(normalizePattern('+33 6 12')).toBe('33612')
    expect(normalizePattern('1')).toBeNull()
  })
})
