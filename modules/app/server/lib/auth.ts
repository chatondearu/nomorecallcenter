import crypto from 'node:crypto'
import process from 'node:process'
import { drizzleAdapter } from '@better-auth/drizzle-adapter'
import { accounts, db, sessions, users, verifications } from '@nmcc/db'
import { betterAuth } from 'better-auth'
import { APIError, createAuthMiddleware } from 'better-auth/api'
import { consumeInvite, countUsers, getValidInviteByToken } from '../utils/invites'

const betterAuthUrl = process.env.BETTER_AUTH_URL || 'http://localhost:3000'
const betterAuthSecret = process.env.BETTER_AUTH_SECRET || 'dev-only-better-auth-secret'
const bootstrapAdminEmail = process.env.BOOTSTRAP_ADMIN_EMAIL?.trim().toLowerCase() || ''

const trustedOrigins = Array.from(new Set([
  betterAuthUrl,
  process.env.NUXT_PUBLIC_BASE_URL?.replace(/\/$/, ''),
  'http://localhost:3000',
  'http://127.0.0.1:3000',
  'http://localhost:3001',
  'http://127.0.0.1:3001',
].filter((value): value is string => Boolean(value))))

function normalizeEmail(email: unknown): string {
  return typeof email === 'string' ? email.trim().toLowerCase() : ''
}

export const auth = betterAuth({
  baseURL: betterAuthUrl,
  secret: betterAuthSecret,
  emailAndPassword: {
    enabled: true,
    disableSignUp: false,
  },
  user: {
    additionalFields: {
      role: {
        type: 'string',
        required: true,
        defaultValue: 'user',
        input: false,
      },
    },
  },
  database: drizzleAdapter(db, {
    provider: 'pg',
    usePlural: true,
    schema: {
      users,
      sessions,
      accounts,
      verifications,
    },
  }),
  databaseHooks: {
    user: {
      create: {
        before: async (user) => {
          const existing = await countUsers()
          const email = normalizeEmail(user.email)

          if (existing === 0) {
            if (bootstrapAdminEmail && email !== bootstrapAdminEmail) {
              throw new APIError('FORBIDDEN', {
                message: 'Only the bootstrap admin email may register on an empty instance',
              })
            }
            return { data: { ...user, role: 'admin' } }
          }

          return { data: { ...user, role: 'user' } }
        },
      },
    },
  },
  hooks: {
    before: createAuthMiddleware(async (ctx) => {
      if (ctx.path !== '/sign-up/email')
        return

      const body = ctx.body as { email?: string, inviteToken?: string } | undefined
      const email = normalizeEmail(body?.email)
      const existing = await countUsers()

      if (existing === 0) {
        if (bootstrapAdminEmail && email !== bootstrapAdminEmail) {
          throw new APIError('FORBIDDEN', {
            message: 'Only the bootstrap admin email may register on an empty instance',
          })
        }
        return
      }

      const inviteToken = typeof body?.inviteToken === 'string' ? body.inviteToken.trim() : ''
      if (!inviteToken) {
        throw new APIError('FORBIDDEN', {
          message: 'Public sign-up is disabled. Use a valid invite token.',
        })
      }

      const invite = await getValidInviteByToken(inviteToken)
      if (!invite) {
        throw new APIError('FORBIDDEN', {
          message: 'Invite not found, used, or expired',
        })
      }

      if (invite.email && normalizeEmail(invite.email) !== email) {
        throw new APIError('FORBIDDEN', {
          message: 'Invite email does not match registration email',
        })
      }
    }),
    after: createAuthMiddleware(async (ctx) => {
      if (ctx.path !== '/sign-up/email')
        return

      const body = ctx.body as { inviteToken?: string } | undefined
      const inviteToken = typeof body?.inviteToken === 'string' ? body.inviteToken.trim() : ''
      const newSession = ctx.context.newSession
      const userId = newSession?.user?.id
      if (!inviteToken || !userId)
        return

      await consumeInvite(inviteToken, userId)
    }),
  },
  advanced: {
    database: {
      generateId: () => crypto.randomUUID(),
    },
  },
  trustedOrigins,
})
