import { callEvents, db } from '@nmcc/db'
import { eq } from 'drizzle-orm'
import { requireAdminSession } from '../../../../utils/auth-session'

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

/**
 * Instance-admin reveal of one event's clear number (ADR-005 / ADR-010).
 * Rows ingested before the column existed return an empty number.
 */
export default defineEventHandler(async (event) => {
  await requireAdminSession(event)
  const id = getRouterParam(event, 'id')
  if (!id || !UUID_RE.test(id)) {
    throw createError({ statusCode: 400, statusMessage: 'Invalid event id' })
  }

  const [row] = await db
    .select({ callerNumber: callEvents.callerNumber })
    .from(callEvents)
    .where(eq(callEvents.id, id))
    .limit(1)

  if (!row) {
    throw createError({ statusCode: 404, statusMessage: 'Event not found' })
  }

  return { number: row.callerNumber ?? '' }
})
