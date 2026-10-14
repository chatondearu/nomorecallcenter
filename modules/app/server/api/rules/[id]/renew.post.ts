import { db, personalRules } from '@nmcc/db'
import { and, eq } from 'drizzle-orm'
import { requireAuthSession } from '../../../utils/auth-session'
import { serializePersonalRule } from '../../../utils/personal-rules'
import { expiresAtForPreset, isTtlPreset, type TtlPreset } from '@nmcc/db'

interface RenewBody {
  ttlPreset?: string
}

export default defineEventHandler(async (event) => {
  const session = await requireAuthSession(event)
  const id = getRouterParam(event, 'id')
  if (!id) {
    throw createError({ statusCode: 400, statusMessage: 'Missing rule id' })
  }

  const body = await readBody<RenewBody>(event).catch(() => ({} as RenewBody))

  const [existing] = await db
    .select()
    .from(personalRules)
    .where(and(
      eq(personalRules.id, id),
      eq(personalRules.userId, session.user.id),
    ))
    .limit(1)

  if (!existing) {
    throw createError({ statusCode: 404, statusMessage: 'Rule not found' })
  }

  let preset: TtlPreset
  if (body.ttlPreset !== undefined) {
    if (!isTtlPreset(body.ttlPreset)) {
      throw createError({
        statusCode: 400,
        statusMessage: 'Invalid ttlPreset (1h|24h|7d|30d|permanent)',
      })
    }
    preset = body.ttlPreset
  }
  else if (isTtlPreset(existing.ttlPreset)) {
    preset = existing.ttlPreset
  }
  else {
    preset = '7d'
  }

  const now = new Date()
  const expiresAt = expiresAtForPreset(preset, now)

  const [row] = await db
    .update(personalRules)
    .set({
      ttlPreset: preset,
      expiresAt,
      updatedAt: now,
    })
    .where(eq(personalRules.id, id))
    .returning()

  if (!row) {
    throw createError({ statusCode: 500, statusMessage: 'Failed to renew rule' })
  }

  return { rule: serializePersonalRule(row, now) }
})
