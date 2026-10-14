import { db, proposals } from '@nmcc/db'
import { eq } from 'drizzle-orm'
import { requireAdminSession } from '../../../utils/auth-session'
import { applyProposalToSharedList } from '../../../utils/list-revision'

export default defineEventHandler(async (event) => {
  const session = await requireAdminSession(event)
  const id = getRouterParam(event, 'id')
  if (!id) {
    throw createError({ statusCode: 400, statusMessage: 'Missing proposal id' })
  }

  const [existing] = await db
    .select()
    .from(proposals)
    .where(eq(proposals.id, id))
    .limit(1)

  if (!existing) {
    throw createError({ statusCode: 404, statusMessage: 'Proposal not found' })
  }

  if (existing.status !== 'pending') {
    throw createError({
      statusCode: 409,
      statusMessage: `Proposal is already ${existing.status}`,
    })
  }

  const revision = await applyProposalToSharedList(existing.pattern)
  const now = new Date()

  const [row] = await db
    .update(proposals)
    .set({
      status: 'approved',
      reviewedBy: session.user.id,
      reviewedAt: now,
      updatedAt: now,
    })
    .where(eq(proposals.id, id))
    .returning()

  if (!row) {
    throw createError({ statusCode: 500, statusMessage: 'Failed to approve proposal' })
  }

  return {
    proposal: {
      id: row.id,
      pattern: row.pattern,
      matchType: row.matchType,
      status: row.status,
      reviewedBy: row.reviewedBy,
      reviewedAt: row.reviewedAt?.toISOString() ?? null,
    },
    listRevision: revision,
  }
})
