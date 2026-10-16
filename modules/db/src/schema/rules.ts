import { pgTable, text, timestamp, unique, uuid } from 'drizzle-orm/pg-core'
import { users } from './auth'

/**
 * Per-account personal rules (allow or block) with TTL presets (ADR-007).
 * expiresAt null means permanent; server is source of truth for sync.
 * The same pattern may exist once as allow and once as block.
 */
export const personalRules = pgTable('personal_rules', {
  id: uuid('id').primaryKey().defaultRandom(),
  userId: uuid('user_id').notNull().references(() => users.id, { onDelete: 'cascade' }),
  pattern: text('pattern').notNull(),
  /** exact | prefix */
  matchType: text('match_type').notNull(),
  /** allow | block */
  effect: text('effect').notNull().default('block'),
  /** 1h | 24h | 7d | 30d | permanent */
  ttlPreset: text('ttl_preset').notNull(),
  expiresAt: timestamp('expires_at', { withTimezone: true, mode: 'date' }),
  createdAt: timestamp('created_at', { withTimezone: true, mode: 'date' }).defaultNow().notNull(),
  updatedAt: timestamp('updated_at', { withTimezone: true, mode: 'date' }).defaultNow().notNull(),
}, table => [
  unique('personal_rules_user_pattern_match_effect').on(table.userId, table.pattern, table.matchType, table.effect),
])

/**
 * Moderated proposals to the shared instance list (ADR-001 / ADR-007).
 * Never auto-publishes — admin approve applies to list_entries.
 */
export const proposals = pgTable('proposals', {
  id: uuid('id').primaryKey().defaultRandom(),
  pattern: text('pattern').notNull(),
  /** exact | prefix — shared list stores pattern as-is (prefix-style matching) */
  matchType: text('match_type').notNull(),
  /** pending | approved | rejected */
  status: text('status').notNull().default('pending'),
  proposedBy: uuid('proposed_by').notNull().references(() => users.id, { onDelete: 'cascade' }),
  personalRuleId: uuid('personal_rule_id').references(() => personalRules.id, { onDelete: 'set null' }),
  reviewedBy: uuid('reviewed_by').references(() => users.id, { onDelete: 'set null' }),
  reviewedAt: timestamp('reviewed_at', { withTimezone: true, mode: 'date' }),
  createdAt: timestamp('created_at', { withTimezone: true, mode: 'date' }).defaultNow().notNull(),
  updatedAt: timestamp('updated_at', { withTimezone: true, mode: 'date' }).defaultNow().notNull(),
})
