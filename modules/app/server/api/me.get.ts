import { requireAuthSession } from '../utils/auth-session'

export default defineEventHandler(async (event) => {
  const session = await requireAuthSession(event)
  return {
    user: {
      id: session.user.id,
      email: session.user.email,
      name: session.user.name,
      role: (session.user as { role?: string }).role ?? 'user',
    },
  }
})
