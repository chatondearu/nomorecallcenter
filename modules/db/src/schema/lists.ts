import { bigint, boolean, pgTable, text, timestamp, uuid } from 'drizzle-orm/pg-core'

/**
 * Shared blocklist entries (prefix patterns) synced to clients.
 * Seeded from ARCEP/data.gouv open data (Licence Ouverte 2.0) — ADR-002.
 */
export const listEntries = pgTable('list_entries', {
  id: uuid('id').primaryKey().defaultRandom(),
  pattern: text('pattern').notNull().unique(),
  source: text('source').notNull(),
  attribution: text('attribution').notNull(),
  sourceDate: text('source_date').notNull(),
  active: boolean('active').notNull().default(true),
  revision: bigint('revision', { mode: 'number' }).notNull(),
  createdAt: timestamp('created_at', { withTimezone: true, mode: 'date' }).defaultNow().notNull(),
  updatedAt: timestamp('updated_at', { withTimezone: true, mode: 'date' }).defaultNow().notNull(),
})
