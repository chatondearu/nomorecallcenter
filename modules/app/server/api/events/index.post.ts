import {
  callEvents,
  db,
  hashCallerNumber,
  isEventAction,
  normalizeCallerNumber,
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
  /** Clear caller number; stored, never returned on this route (ADR-010). */
  number?: unknown
  /** Forbidden aliases — presence → 400 */
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
 * Ingest a call screening event.
 * When `number` is set, store the clear normalized digits and a SHA-256 display hash.
 * The response never includes the clear number (ADR-010).
 */
export default defineEventHandler(async (event) => {
  const session = await requireAuthSession(event)
  const body = await readBody<IngestBody>(event).catch(() => ({} as IngestBody))

  if (body.phone != null || body.fullNumber != null) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Use number for the clear caller; phone and fullNumber are not accepted',
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

  let callerNumber: string | null = null
  if (body.number != null && body.number !== '') {
    callerNumber = normalizeCallerNumber(body.number)
    if (callerNumber == null) {
      throw createError({
        statusCode: 400,
        statusMessage: 'Invalid number (expected 2–15 digits)',
      })
    }
  }

  const hashRaw = body.hash ?? body.numberHash
  const numberHash = callerNumber != null
    ? hashCallerNumber(callerNumber)
    : (hashRaw != null && hashRaw !== '' ? normalizeEventHash(hashRaw) : null)
  if (callerNumber == null && hashRaw != null && hashRaw !== '' && numberHash == null) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Invalid hash (expected SHA-256 hex)',
    })
  }

  if (prefix == null && numberHash == null) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Provide prefix, hash, and/or number',
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
    callerNumber,
    action: body.action,
    occurredAt,
  }).returning()

  if (!row) {
    throw createError({ statusCode: 500, statusMessage: 'Failed to ingest event' })
  }

  return { event: serializeEvent(row) }
})
