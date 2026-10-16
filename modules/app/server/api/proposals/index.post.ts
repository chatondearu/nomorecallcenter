import { db, personalRules, proposals } from '@nmcc/db'
import { and, eq } from 'drizzle-orm'
import { requireAuthSession } from '../../utils/auth-session'
import { isMatchType, normalizePattern } from '@nmcc/db'

interface ProposeBody {
  personalRuleId?: string
  pattern?: string
  matchType?: string
}

function serializeProposal(row: typeof proposals.$inferSelect) {
  return {
    id: row.id,
    pattern: row.pattern,
    matchType: row.matchType,
    status: row.status,
    proposedBy: row.proposedBy,
    personalRuleId: row.personalRuleId,
    createdAt: row.createdAt.toISOString(),
    updatedAt: row.updatedAt.toISOString(),
  }
}

/**
 * Create a pending proposal to the shared list.
 * Does not mutate list_entries until admin approve (ADR-007).
 */
export default defineEventHandler(async (event) => {
  const session = await requireAuthSession(event)
  const body = await readBody<ProposeBody>(event).catch(() => ({} as ProposeBody))
  const now = new Date()

  let pattern: string
  let matchType: string
  let personalRuleId: string | null = null

  if (typeof body.personalRuleId === 'string' && body.personalRuleId) {
    const [rule] = await db
      .select()
      .from(personalRules)
      .where(and(
        eq(personalRules.id, body.personalRuleId),
        eq(personalRules.userId, session.user.id),
      ))
      .limit(1)

    if (!rule) {
      throw createError({ statusCode: 404, statusMessage: 'Personal rule not found' })
    }

    if (rule.effect === 'allow') {
      throw createError({
        statusCode: 400,
        statusMessage: 'Allow rules stay personal; shared lists are block-only',
      })
    }

    pattern = rule.pattern
    matchType = rule.matchType
    personalRuleId = rule.id
  }
  else {
    const normalized = normalizePattern(body.pattern)
    if (!normalized) {
      throw createError({
        statusCode: 400,
        statusMessage: 'Invalid pattern (or provide personalRuleId)',
      })
    }
    if (!isMatchType(body.matchType)) {
      throw createError({
        statusCode: 400,
        statusMessage: 'Invalid matchType (exact|prefix)',
      })
    }
    pattern = normalized
    matchType = body.matchType
  }

  const [row] = await db.insert(proposals).values({
    pattern,
    matchType,
    status: 'pending',
    proposedBy: session.user.id,
    personalRuleId,
    createdAt: now,
    updatedAt: now,
  }).returning()

  if (!row) {
    throw createError({ statusCode: 500, statusMessage: 'Failed to create proposal' })
  }

  return { proposal: serializeProposal(row) }
})
