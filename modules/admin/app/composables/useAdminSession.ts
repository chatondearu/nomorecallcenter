export interface AdminUser {
  id: string
  email: string
  name: string
  role: string
}

export function useAdminSession() {
  const user = useState<AdminUser | null>('admin-user', () => null)
  const { apiFetch } = useApi()

  async function refresh(): Promise<AdminUser | null> {
    try {
      const res = await apiFetch<{ user: AdminUser }>('/api/me')
      user.value = res.user
      return res.user
    }
    catch {
      user.value = null
      return null
    }
  }

  async function signIn(email: string, password: string) {
    await apiFetch('/api/auth/sign-in/email', {
      method: 'POST',
      body: { email, password },
    })
    return refresh()
  }

  async function signOut() {
    try {
      await apiFetch('/api/auth/sign-out', { method: 'POST' })
    }
    catch {
      // ignore — clear local state anyway
    }
    user.value = null
  }

  const isAdmin = computed(() => user.value?.role === 'admin')

  return { user, isAdmin, refresh, signIn, signOut }
}
