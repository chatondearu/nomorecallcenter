import { db, personalRules } from '@nmcc/db'
import { and, eq } from 'drizzle-orm'
import { requireAuthSession } from '../../utils/auth-session'

export default defineEventHandler(async (event) => {
  const session = await requireAuthSession(event)
  const id = getRouterParam(event, 'id')
  if (!id) {
    throw createError({ statusCode: 400, statusMessage: 'Missing rule id' })
  }

  const deleted = await db
    .delete(personalRules)
    .where(and(
      eq(personalRules.id, id),
      eq(personalRules.userId, session.user.id),
    ))
    .returning({ id: personalRules.id })

  if (deleted.length === 0) {
    throw createError({ statusCode: 404, statusMessage: 'Rule not found' })
  }

  return { ok: true, id }
})
