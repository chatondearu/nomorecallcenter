import { callEvents, db } from '@nmcc/db'
import type { EventAction } from '@nmcc/db'
import { and, desc, eq, gte, isNotNull, sql } from 'drizzle-orm'

const EMPTY_COUNTERS: Record<EventAction, number> = {
  screened: 0,
  blocked: 0,
  allowed: 0,
}

function emptyWindows() {
  return {
    '7d': { ...EMPTY_COUNTERS },
    '30d': { ...EMPTY_COUNTERS },
  }
}

async function countByAction(userId: string | null, since: Date) {
  const conditions = [gte(callEvents.occurredAt, since)]
  if (userId)
    conditions.push(eq(callEvents.userId, userId))

  const rows = await db
    .select({
      action: callEvents.action,
      count: sql<number>`cast(count(*) as int)`,
    })
    .from(callEvents)
    .where(and(...conditions))
    .groupBy(callEvents.action)

  const out = { ...EMPTY_COUNTERS }
  for (const row of rows) {
    if (row.action in out)
      out[row.action as EventAction] = Number(row.count)
  }
  return out
}

async function topPrefixes(userId: string | null, since: Date, limit = 10) {
  const conditions = [
    gte(callEvents.occurredAt, since),
    isNotNull(callEvents.prefix),
  ]
  if (userId)
    conditions.push(eq(callEvents.userId, userId))

  const rows = await db
    .select({
      prefix: callEvents.prefix,
      count: sql<number>`cast(count(*) as int)`,
    })
    .from(callEvents)
    .where(and(...conditions))
    .groupBy(callEvents.prefix)
    .orderBy(desc(sql`count(*)`))
    .limit(limit)

  return rows
    .filter(r => r.prefix != null)
    .map(r => ({ prefix: r.prefix as string, count: Number(r.count) }))
}

async function recentHistory(userId: string | null, since: Date, limit = 50) {
  const conditions = [gte(callEvents.occurredAt, since)]
  if (userId)
    conditions.push(eq(callEvents.userId, userId))

  const rows = await db
    .select({
      id: callEvents.id,
      prefix: callEvents.prefix,
      numberHash: callEvents.numberHash,
      action: callEvents.action,
      occurredAt: callEvents.occurredAt,
      userId: callEvents.userId,
    })
    .from(callEvents)
    .where(and(...conditions))
    .orderBy(desc(callEvents.occurredAt))
    .limit(limit)

  return rows.map(r => ({
    id: r.id,
    prefix: r.prefix,
    hash: r.numberHash,
    action: r.action,
    occurredAt: r.occurredAt.toISOString(),
    ...(userId ? {} : { userId: r.userId }),
  }))
}

export async function buildStatsBundle(userId: string | null, now: Date, cutoff: Date) {
  const since7d = new Date(Math.max(cutoff.getTime(), now.getTime() - 7 * 86_400_000))
  const since30d = new Date(Math.max(cutoff.getTime(), now.getTime() - 30 * 86_400_000))

  const [c7, c30, top, history] = await Promise.all([
    countByAction(userId, since7d),
    countByAction(userId, since30d),
    topPrefixes(userId, since30d),
    recentHistory(userId, cutoff),
  ])

  const counters = emptyWindows()
  counters['7d'] = c7
  counters['30d'] = c30

  return {
    counters,
    topPrefixes: top,
    recent: history,
  }
}

export async function eventVolume(since: Date) {
  const [row] = await db
    .select({ count: sql<number>`cast(count(*) as int)` })
    .from(callEvents)
    .where(gte(callEvents.occurredAt, since))
  return Number(row?.count ?? 0)
}
