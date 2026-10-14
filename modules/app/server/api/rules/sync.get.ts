import { db, personalRules } from '@nmcc/db'
import { and, eq, gt, isNull, or } from 'drizzle-orm'
import { requireAuthSession } from '../../utils/auth-session'
import { serializePersonalRule } from '../../utils/personal-rules'

/**
 * Authenticated personal-rules sync for the account.
 * Returns only active rules (permanent or expiresAt > now).
 */
export default defineEventHandler(async (event) => {
  const session = await requireAuthSession(event)
  const now = new Date()

  const rows = await db
    .select()
    .from(personalRules)
    .where(and(
      eq(personalRules.userId, session.user.id),
      or(
        isNull(personalRules.expiresAt),
        gt(personalRules.expiresAt, now),
      ),
    ))

  return {
    rules: rows.map(row => serializePersonalRule(row, now)),
  }
})
