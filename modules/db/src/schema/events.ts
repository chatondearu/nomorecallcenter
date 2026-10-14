import { index, pgTable, text, timestamp, uuid } from 'drizzle-orm/pg-core'
import { users } from './auth'

/**
 * Call screening events (ADR-001 / ADR-006).
 * Store prefix and/or hash only — never clear full phone numbers.
 */
export const callEvents = pgTable('call_events', {
  id: uuid('id').primaryKey().defaultRandom(),
  userId: uuid('user_id').notNull().references(() => users.id, { onDelete: 'cascade' }),
  /** Digits-only prefix (2–8); null when hash-only */
  prefix: text('prefix'),
  /** SHA-256 hex of the handle (optional when prefix-only) */
  numberHash: text('number_hash'),
  /** screened | blocked | allowed */
  action: text('action').notNull(),
  occurredAt: timestamp('occurred_at', { withTimezone: true, mode: 'date' }).notNull(),
  createdAt: timestamp('created_at', { withTimezone: true, mode: 'date' }).defaultNow().notNull(),
}, table => [
  index('call_events_user_occurred_idx').on(table.userId, table.occurredAt),
  index('call_events_occurred_idx').on(table.occurredAt),
])
