import { db, proposals } from '@nmcc/db'
import { sql } from 'drizzle-orm'
import { requireAuthSession } from '../../utils/auth-session'
import { purgeExpiredEvents } from '../../utils/events-retention'
import { buildStatsBundle, eventVolume } from '../../utils/stats-aggregate'

/**
 * Instance-wide aggregates for members.
 * Admin also gets proposal funnel + event volume (ADR-006).
 */
export default defineEventHandler(async (event) => {
  const session = await requireAuthSession(event)
  const now = new Date()
  const cutoff = await purgeExpiredEvents(now)
  const stats = await buildStatsBundle(null, now, cutoff)
  const role = (session.user as { role?: string }).role
  const isAdmin = role === 'admin'

  const body: Record<string, unknown> = {
    scope: 'instance' as const,
    retentionCutoff: cutoff.toISOString(),
    ...stats,
    // Members must not see per-user ids on the instance recent list
    recent: stats.recent.map(({ userId: _uid, ...rest }) => rest),
  }

  if (isAdmin) {
    const funnelRows = await db
      .select({
        status: proposals.status,
        count: sql<number>`cast(count(*) as int)`,
      })
      .from(proposals)
      .groupBy(proposals.status)

    const funnel = { submitted: 0, pending: 0, approved: 0, rejected: 0 }
    for (const row of funnelRows) {
      const n = Number(row.count)
      if (row.status === 'pending') {
        funnel.pending = n
        funnel.submitted += n
      }
      else if (row.status === 'approved') {
        funnel.approved = n
        funnel.submitted += n
      }
      else if (row.status === 'rejected') {
        funnel.rejected = n
        funnel.submitted += n
      }
    }

    body.proposalFunnel = funnel
    body.eventVolume = await eventVolume(cutoff)
  }

  return body
})
