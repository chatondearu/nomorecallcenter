import { callEvents, db, retentionCutoff } from '@nmcc/db'
import { lt } from 'drizzle-orm'

/** Lazy purge of events older than the configured retention window (clear number included). */
export async function purgeExpiredEvents(now: Date = new Date()) {
  const cutoff = retentionCutoff(now)
  await db.delete(callEvents).where(lt(callEvents.occurredAt, cutoff))
  return cutoff
}
