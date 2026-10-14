/**
 * Fetch against the API. Default base is empty (Nitro proxies /api → API).
 */
export function useApi() {
  const config = useRuntimeConfig()
  const base = String(config.public.apiBase || '')

  async function apiFetch<T>(path: string, opts: Parameters<typeof $fetch<T>>[1] = {}) {
    const headers = import.meta.server
      ? useRequestHeaders(['cookie'])
      : undefined

    return $fetch<T>(`${base}${path}`, {
      credentials: 'include',
      ...opts,
      headers: {
        ...(headers || {}),
        ...(opts?.headers as Record<string, string> | undefined),
      },
    })
  }

  return { apiFetch, apiBase: base }
}
