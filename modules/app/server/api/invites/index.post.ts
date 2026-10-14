import process from 'node:process'
import { db, invites } from '@nmcc/db'
import { requireAdminSession } from '../../utils/auth-session'
import { createInviteToken } from '../../utils/invites'

interface CreateInviteBody {
  email?: string
  expiresInDays?: number
}

export default defineEventHandler(async (event) => {
  const session = await requireAdminSession(event)
  const body = await readBody<CreateInviteBody>(event).catch(() => ({} as CreateInviteBody))

  const expiresInDays = body.expiresInDays ?? 7
  if (!Number.isFinite(expiresInDays) || expiresInDays <= 0 || expiresInDays > 365) {
    throw createError({ statusCode: 400, statusMessage: 'expiresInDays must be between 1 and 365' })
  }

  const expiresAt = new Date(Date.now() + expiresInDays * 86_400_000)
  const token = createInviteToken()
  const email = typeof body.email === 'string' && body.email.trim()
    ? body.email.trim().toLowerCase()
    : null

  const [invite] = await db.insert(invites).values({
    token,
    email,
    createdBy: session.user.id,
    expiresAt,
  }).returning()

  if (!invite) {
    throw createError({ statusCode: 500, statusMessage: 'Failed to create invite' })
  }

  const baseUrl = (process.env.BETTER_AUTH_URL || 'http://localhost:3000').replace(/\/$/, '')

  return {
    invite: {
      id: invite.id,
      email: invite.email,
      expiresAt: invite.expiresAt,
    },
    token,
    url: `${baseUrl}/api/auth/sign-up/email?invite=${encodeURIComponent(token)}`,
  }
})
