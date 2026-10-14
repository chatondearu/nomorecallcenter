import { LISTS_REVISION_KEY, db, listEntries, schemaMeta } from '@nmcc/db'
import { and, eq, gt } from 'drizzle-orm'
import { requireAuthSession } from '../../utils/auth-session'

/**
 * Authenticated list sync.
 * - since=0 (default): full snapshot of active entries
 * - since>0: entries with revision > since (includes inactive tombstones)
 */
export default defineEventHandler(async (event) => {
  await requireAuthSession(event)

  const query = getQuery(event)
  const sinceRaw = typeof query.since === 'string' ? query.since : '0'
  const since = Number.parseInt(sinceRaw, 10)
  if (!Number.isFinite(since) || since < 0) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Invalid since (expected non-negative integer)',
    })
  }

  const [meta] = await db
    .select()
    .from(schemaMeta)
    .where(eq(schemaMeta.key, LISTS_REVISION_KEY))
    .limit(1)

  const revision = meta ? Number(meta.value) : 0

  const rows = since === 0
    ? await db
      .select()
      .from(listEntries)
      .where(eq(listEntries.active, true))
    : await db
      .select()
      .from(listEntries)
      .where(and(gt(listEntries.revision, since)))

  return {
    revision,
    since,
    entries: rows.map(row => ({
      id: row.id,
      pattern: row.pattern,
      source: row.source,
      attribution: row.attribution,
      sourceDate: row.sourceDate,
      active: row.active,
      revision: row.revision,
    })),
  }
})
