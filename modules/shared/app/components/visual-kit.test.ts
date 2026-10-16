import { readFileSync, readdirSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const dir = dirname(fileURLToPath(import.meta.url))

function source(name: string) {
  return readFileSync(join(dir, name), 'utf8')
}

describe('shared visual kit', () => {
  it('protection card owns the French shield copy', () => {
    const card = source('ProtectionCard.vue')
    expect(card).toContain('Protection active')
    expect(card).toContain('Rejet silencieux')
    expect(card).not.toMatch(/SQLite|hors ligne|offline/i)
  })

  it('ttl chips use primary-container for the active option', () => {
    const chips = source('TtlChips.vue')
    for (const label of ['1h', '24h', '7j', '30j', 'Permanent'])
      expect(chips).toContain(label)
    expect(chips).toContain('bg-system-primary-container')
  })

  it('vue templates stay on token classes', () => {
    for (const file of readdirSync(dir).filter(name => name.endsWith('.vue')))
      expect(source(file), file).not.toMatch(/#[0-9a-fA-F]{3,8}/)
  })
})
