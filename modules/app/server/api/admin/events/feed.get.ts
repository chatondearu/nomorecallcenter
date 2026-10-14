import { callEvents, db, retentionCutoff } from '@nmcc/db'
import { desc, gte } from 'drizzle-orm'
import { requireAdminSession } from '../../../utils/auth-session'
import { purgeExpiredEvents } from '../../../utils/events-retention'

/**
 * Admin-only near-live poll feed of recent instance events (ADR-006).
 */
export default defineEventHandler(async (event) => {
  await requireAdminSession(event)
  const now = new Date()
  await purgeExpiredEvents(now)
  const cutoff = retentionCutoff(now)

  const query = getQuery(event)
  const limitRaw = Number(query.limit ?? 50)
  const limit = Number.isFinite(limitRaw)
    ? Math.min(Math.max(Math.floor(limitRaw), 1), 200)
    : 50

  const rows = await db
    .select({
      id: callEvents.id,
      prefix: callEvents.prefix,
      numberHash: callEvents.numberHash,
      action: callEvents.action,
      userId: callEvents.userId,
      occurredAt: callEvents.occurredAt,
    })
    .from(callEvents)
    .where(gte(callEvents.occurredAt, cutoff))
    .orderBy(desc(callEvents.occurredAt))
    .limit(limit)

  return {
    retentionCutoff: cutoff.toISOString(),
    events: rows.map(r => ({
      id: r.id,
      prefix: r.prefix,
      hash: r.numberHash,
      action: r.action,
      userId: r.userId,
      occurredAt: r.occurredAt.toISOString(),
    })),
  }
})
