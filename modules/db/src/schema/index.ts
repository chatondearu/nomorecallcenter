export * from './auth'
export * from './invites'
export * from './lists'
export * from './rules'

import { pgTable, text, timestamp } from 'drizzle-orm/pg-core'

/**
 * Key/value meta (bootstrap + list sync revision counter).
 */
export const schemaMeta = pgTable('schema_meta', {
  key: text('key').primaryKey(),
  value: text('value').notNull(),
  updatedAt: timestamp('updated_at', { withTimezone: true }).defaultNow().notNull(),
})

/** schema_meta key for shared list sync revision */
export const LISTS_REVISION_KEY = 'lists.revision'
