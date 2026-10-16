import { index, pgTable, text, timestamp, uuid } from 'drizzle-orm/pg-core'
import { users } from './auth'

/**
 * Call screening events (ADR-010).
 * callerNumber is the clear normalized number. Reads expose numberHash (SHA-256 hex) unless an admin reveals one event.
 */
export const callEvents = pgTable('call_events', {
  id: uuid('id').primaryKey().defaultRandom(),
  userId: uuid('user_id').notNull().references(() => users.id, { onDelete: 'cascade' }),
  /** Digits-only prefix (2–8); null when hash-only */
  prefix: text('prefix'),
  /** SHA-256 hex of the normalized digits */
  numberHash: text('number_hash'),
  /** Clear normalized digits; null on rows ingested before ADR-010 */
  callerNumber: text('caller_number'),
  /** screened | blocked | allowed */
  action: text('action').notNull(),
  occurredAt: timestamp('occurred_at', { withTimezone: true, mode: 'date' }).notNull(),
  createdAt: timestamp('created_at', { withTimezone: true, mode: 'date' }).defaultNow().notNull(),
}, table => [
  index('call_events_user_occurred_idx').on(table.userId, table.occurredAt),
  index('call_events_occurred_idx').on(table.occurredAt),
])
