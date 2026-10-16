import { db, personalRules } from '@nmcc/db'
import { requireAuthSession } from '../../utils/auth-session'
import { serializePersonalRule } from '../../utils/personal-rules'
import { expiresAtForPreset, isMatchType, isRuleEffect, isTtlPreset, normalizePattern } from '@nmcc/db'

interface CreateRuleBody {
  pattern?: string
  matchType?: string
  ttlPreset?: string
  /** allow | block; omitted means block */
  effect?: string
}

export default defineEventHandler(async (event) => {
  const session = await requireAuthSession(event)
  const body = await readBody<CreateRuleBody>(event).catch(() => ({} as CreateRuleBody))

  const pattern = normalizePattern(body.pattern)
  if (!pattern) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Invalid pattern (expected 2–15 digits)',
    })
  }

  if (!isMatchType(body.matchType)) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Invalid matchType (exact|prefix)',
    })
  }

  if (!isTtlPreset(body.ttlPreset)) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Invalid ttlPreset (1h|24h|7d|30d|permanent)',
    })
  }

  const effect = body.effect == null || body.effect === '' ? 'block' : body.effect
  if (!isRuleEffect(effect)) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Invalid effect (allow|block)',
    })
  }

  const now = new Date()
  const expiresAt = expiresAtForPreset(body.ttlPreset, now)

  try {
    const [row] = await db.insert(personalRules).values({
      userId: session.user.id,
      pattern,
      matchType: body.matchType,
      effect,
      ttlPreset: body.ttlPreset,
      expiresAt,
      createdAt: now,
      updatedAt: now,
    }).returning()

    if (!row) {
      throw createError({ statusCode: 500, statusMessage: 'Failed to create rule' })
    }

    return { rule: serializePersonalRule(row, now) }
  }
  catch (error: unknown) {
    const wrapped = error as { code?: unknown, cause?: { code?: unknown } }
    const code = String(wrapped?.code ?? wrapped?.cause?.code ?? '')
    if (code === '23505') {
      throw createError({
        statusCode: 409,
        statusMessage: 'Rule already exists for this pattern, matchType, and effect',
      })
    }
    throw error
  }
})
