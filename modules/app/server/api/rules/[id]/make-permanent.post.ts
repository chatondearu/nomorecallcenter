import { db, personalRules } from '@nmcc/db'
import { and, eq } from 'drizzle-orm'
import { requireAuthSession } from '../../../utils/auth-session'
import { serializePersonalRule } from '../../../utils/personal-rules'

export default defineEventHandler(async (event) => {
  const session = await requireAuthSession(event)
  const id = getRouterParam(event, 'id')
  if (!id) {
    throw createError({ statusCode: 400, statusMessage: 'Missing rule id' })
  }

  const now = new Date()
  const [row] = await db
    .update(personalRules)
    .set({
      ttlPreset: 'permanent',
      expiresAt: null,
      updatedAt: now,
    })
    .where(and(
      eq(personalRules.id, id),
      eq(personalRules.userId, session.user.id),
    ))
    .returning()

  if (!row) {
    throw createError({ statusCode: 404, statusMessage: 'Rule not found' })
  }

  return { rule: serializePersonalRule(row, now) }
})
