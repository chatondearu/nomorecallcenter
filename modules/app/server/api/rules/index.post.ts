import { db, personalRules } from '@nmcc/db'
import { requireAuthSession } from '../../utils/auth-session'
import { serializePersonalRule } from '../../utils/personal-rules'
import { expiresAtForPreset, isMatchType, isTtlPreset, normalizePattern } from '@nmcc/db'

interface CreateRuleBody {
  pattern?: string
  matchType?: string
  ttlPreset?: string
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

  const now = new Date()
  const expiresAt = expiresAtForPreset(body.ttlPreset, now)

  try {
    const [row] = await db.insert(personalRules).values({
      userId: session.user.id,
      pattern,
      matchType: body.matchType,
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
    const code = typeof error === 'object' && error && 'code' in error
      ? String((error as { code: unknown }).code)
      : ''
    if (code === '23505') {
      throw createError({
        statusCode: 409,
        statusMessage: 'Rule already exists for this pattern and matchType',
      })
    }
    throw error
  }
})
