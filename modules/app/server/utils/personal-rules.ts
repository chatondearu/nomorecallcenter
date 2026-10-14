import type { personalRules } from '@nmcc/db'
import { isRuleActive } from '@nmcc/db'

type PersonalRuleRow = typeof personalRules.$inferSelect

export function serializePersonalRule(row: PersonalRuleRow, now: Date = new Date()) {
  return {
    id: row.id,
    pattern: row.pattern,
    matchType: row.matchType,
    ttlPreset: row.ttlPreset,
    expiresAt: row.expiresAt ? row.expiresAt.toISOString() : null,
    active: isRuleActive(row.expiresAt, now),
    createdAt: row.createdAt.toISOString(),
    updatedAt: row.updatedAt.toISOString(),
  }
}
