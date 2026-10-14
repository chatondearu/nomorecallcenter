import {
  callEvents,
  db,
  isEventAction,
  normalizeEventHash,
  normalizeEventPrefix,
} from '@nmcc/db'
import { requireAuthSession } from '../../utils/auth-session'
import { purgeExpiredEvents } from '../../utils/events-retention'

interface IngestBody {
  prefix?: string
  hash?: string
  numberHash?: string
  action?: string
  occurredAt?: string
  /** Forbidden clear-number fields — presence → 400 */
  number?: unknown
  phone?: unknown
  fullNumber?: unknown
}

function serializeEvent(row: typeof callEvents.$inferSelect) {
  return {
    id: row.id,
    prefix: row.prefix,
    hash: row.numberHash,
    action: row.action,
    occurredAt: row.occurredAt.toISOString(),
    userId: row.userId,
  }
}

/**
 * Ingest a call screening event (prefix/hash only — ADR-001).
 */
export default defineEventHandler(async (event) => {
  const session = await requireAuthSession(event)
  const body = await readBody<IngestBody>(event).catch(() => ({} as IngestBody))

  if (body.number != null || body.phone != null || body.fullNumber != null) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Clear phone numbers are not accepted; send prefix and/or hash only',
    })
  }

  if (!isEventAction(body.action)) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Invalid action (screened|blocked|allowed)',
    })
  }

  const prefix = body.prefix != null && body.prefix !== ''
    ? normalizeEventPrefix(body.prefix)
    : null
  if (body.prefix != null && body.prefix !== '' && prefix == null) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Invalid prefix (2–8 digits; clear full numbers rejected)',
    })
  }

  const hashRaw = body.hash ?? body.numberHash
  const numberHash = hashRaw != null && hashRaw !== ''
    ? normalizeEventHash(hashRaw)
    : null
  if (hashRaw != null && hashRaw !== '' && numberHash == null) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Invalid hash (expected SHA-256 hex)',
    })
  }

  if (prefix == null && numberHash == null) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Provide prefix and/or hash',
    })
  }

  let occurredAt = new Date()
  if (body.occurredAt != null && body.occurredAt !== '') {
    const parsed = new Date(body.occurredAt)
    if (Number.isNaN(parsed.getTime())) {
      throw createError({ statusCode: 400, statusMessage: 'Invalid occurredAt' })
    }
    occurredAt = parsed
  }

  await purgeExpiredEvents()

  const [row] = await db.insert(callEvents).values({
    userId: session.user.id,
    prefix,
    numberHash,
    action: body.action,
    occurredAt,
  }).returning()

  if (!row) {
    throw createError({ statusCode: 500, statusMessage: 'Failed to ingest event' })
  }

  return { event: serializeEvent(row) }
})
