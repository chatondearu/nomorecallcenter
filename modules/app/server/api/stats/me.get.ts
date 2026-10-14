import { requireAuthSession } from '../../utils/auth-session'
import { purgeExpiredEvents } from '../../utils/events-retention'
import { buildStatsBundle } from '../../utils/stats-aggregate'

/**
 * Personal stats: counters 7d/30d, top prefixes, recent history (ADR-006).
 */
export default defineEventHandler(async (event) => {
  const session = await requireAuthSession(event)
  const now = new Date()
  const cutoff = await purgeExpiredEvents(now)
  const stats = await buildStatsBundle(session.user.id, now, cutoff)

  return {
    scope: 'personal' as const,
    retentionCutoff: cutoff.toISOString(),
    ...stats,
  }
})
