import { pgTable, text, timestamp, uuid } from 'drizzle-orm/pg-core'
import { users } from './auth'

export const invites = pgTable('invites', {
  id: uuid('id').primaryKey().defaultRandom(),
  token: text('token').notNull().unique(),
  email: text('email'),
  createdBy: uuid('created_by').notNull().references(() => users.id, { onDelete: 'cascade' }),
  expiresAt: timestamp('expires_at', { withTimezone: true, mode: 'date' }).notNull(),
  usedAt: timestamp('used_at', { withTimezone: true, mode: 'date' }),
  usedByUserId: uuid('used_by_user_id').references(() => users.id, { onDelete: 'set null' }),
  createdAt: timestamp('created_at', { withTimezone: true, mode: 'date' }).defaultNow().notNull(),
})
