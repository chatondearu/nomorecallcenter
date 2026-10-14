export default defineNuxtRouteMiddleware(async (to) => {
  if (to.path === '/login')
    return

  const { user, refresh } = useAdminSession()
  if (!user.value)
    await refresh()

  if (!user.value) {
    return navigateTo({
      path: '/login',
      query: { redirect: to.fullPath },
    })
  }

  if (user.value.role !== 'admin') {
    return navigateTo('/login?error=admin')
  }
})
