import { randomBytes } from 'node:crypto'
import { and, count, eq, isNull, sql } from 'drizzle-orm'
import { db, invites, users } from '@nmcc/db'

export function createInviteToken(): string {
  return randomBytes(24).toString('base64url')
}

export async function countUsers(): Promise<number> {
  const [row] = await db.select({ value: count() }).from(users)
  return row?.value ?? 0
}

export async function getValidInviteByToken(token: string) {
  const [invite] = await db
    .select()
    .from(invites)
    .where(and(
      eq(invites.token, token),
      isNull(invites.usedAt),
      sql`${invites.expiresAt} > now()`,
    ))
    .limit(1)

  return invite ?? null
}

export async function consumeInvite(token: string, userId: string) {
  const invite = await getValidInviteByToken(token)
  if (!invite)
    return null

  const [updated] = await db
    .update(invites)
    .set({
      usedAt: new Date(),
      usedByUserId: userId,
    })
    .where(and(eq(invites.id, invite.id), isNull(invites.usedAt)))
    .returning()

  return updated ?? null
}
