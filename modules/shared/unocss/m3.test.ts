import { describe, expect, it } from 'vitest'
import tokens from '../../../design/tokens/md3-colors.json'
import { themeFromSeed } from './m3'

describe('themeFromSeed', () => {
  it('uses DESIGN.md seed #1A6352 and produces light/dark schemes', () => {
    expect(tokens.seed).toBe('#1A6352')
    const theme = themeFromSeed(tokens.seed)
    expect(theme.seed).toBe('#1A6352')
    expect(theme.light.primary).toBeTypeOf('number')
    expect(theme.dark.primary).toBeTypeOf('number')
    expect(theme.palettes.primary.DEFAULT).toMatch(/^#/)
    expect(theme.semantic.statusBlocked).toBe(tokens.semantic.statusBlocked)
  })
})
