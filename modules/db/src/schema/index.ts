import { pgTable, text, timestamp } from 'drizzle-orm/pg-core'

/**
 * Bootstrap table so Slice 0 can migrate a fresh DB.
 * Domain tables (auth, lists, rules, events) land in later slices.
 */
export const schemaMeta = pgTable('schema_meta', {
  key: text('key').primaryKey(),
  value: text('value').notNull(),
  updatedAt: timestamp('updated_at', { withTimezone: true }).defaultNow().notNull(),
})
