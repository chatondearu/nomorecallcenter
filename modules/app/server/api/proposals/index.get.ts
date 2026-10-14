import { db, proposals } from '@nmcc/db'
import { eq } from 'drizzle-orm'
import { requireAdminSession } from '../../utils/auth-session'

/**
 * Admin: list proposals (default pending).
 */
export default defineEventHandler(async (event) => {
  await requireAdminSession(event)

  const query = getQuery(event)
  const statusRaw = typeof query.status === 'string' ? query.status : 'pending'
  if (!['pending', 'approved', 'rejected', 'all'].includes(statusRaw)) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Invalid status (pending|approved|rejected|all)',
    })
  }

  const rows = statusRaw === 'all'
    ? await db.select().from(proposals)
    : await db.select().from(proposals).where(eq(proposals.status, statusRaw))

  return {
    proposals: rows.map(row => ({
      id: row.id,
      pattern: row.pattern,
      matchType: row.matchType,
      status: row.status,
      proposedBy: row.proposedBy,
      personalRuleId: row.personalRuleId,
      reviewedBy: row.reviewedBy,
      reviewedAt: row.reviewedAt ? row.reviewedAt.toISOString() : null,
      createdAt: row.createdAt.toISOString(),
      updatedAt: row.updatedAt.toISOString(),
    })),
  }
})
