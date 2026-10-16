<script setup lang="ts">
definePageMeta({
  middleware: 'admin',
})

interface FeedEvent {
  id: string
  prefix: string | null
  hash: string | null
  action: string
  userId: string
  occurredAt: string
}

const actionLabels: Record<string, string> = {
  screened: 'Filtré',
  blocked: 'Bloqué',
  allowed: 'Autorisé',
}

const { apiFetch } = useApi()
const events = ref<FeedEvent[]>([])
const revealed = ref<Record<string, string>>({})
const error = ref<string | null>(null)
const loading = ref(true)

function hashMarker(hash: string | null) {
  if (!hash)
    return '—'
  return hash.length > 12 ? `${hash.slice(0, 12)}…` : hash
}

function statusLabel(action: string) {
  return actionLabels[action] || action
}

async function reveal(id: string) {
  try {
    const body = await apiFetch<{ number: string }>(`/api/admin/events/${id}/reveal`, { method: 'POST' })
    revealed.value = { ...revealed.value, [id]: body.number || '(vide)' }
  }
  catch (e: unknown) {
    const err = e as { statusMessage?: string }
    error.value = err?.statusMessage || 'Révélation refusée.'
  }
}

async function load() {
  try {
    const feed = await apiFetch<{ events: FeedEvent[] }>('/api/admin/events/feed?limit=40')
    events.value = feed.events
    error.value = null
  }
  catch (e: unknown) {
    const err = e as { statusMessage?: string }
    error.value = err?.statusMessage || 'Impossible de charger le fil.'
  }
  finally {
    loading.value = false
  }
}

await load()

let timer: ReturnType<typeof setInterval> | undefined
onMounted(() => {
  timer = setInterval(() => {
    void load()
  }, 5000)
})
onUnmounted(() => {
  if (timer)
    clearInterval(timer)
})

function formatTime(iso: string) {
  try {
    return new Date(iso).toLocaleString('fr-FR')
  }
  catch {
    return iso
  }
}
</script>

<template>
  <div class="flex flex-col gap-lg max-w-4xl">
    <header>
      <h1 class="text-2xl font-bold text-system-on-surface">
        Événements
      </h1>
      <p class="text-system-on-surface-variant text-sm mt-xs">
        Fil d’événements (empreinte), rafraîchi toutes les 5 s. La révélation concerne un seul événement.
      </p>
    </header>

    <p v-if="error" class="text-error text-sm" role="alert">
      {{ error }}
    </p>
    <p v-if="loading" class="text-system-on-surface-variant text-sm">
      Chargement…
    </p>

    <ul v-else-if="events.length" class="flex flex-col gap-sm">
      <li
        v-for="ev in events"
        :key="ev.id"
        class="flex flex-col gap-xs"
      >
        <EventRow
          :title="hashMarker(ev.hash)"
          :status="statusLabel(ev.action)"
          :reason="ev.prefix || ''"
          :time="formatTime(ev.occurredAt)"
        />
        <div class="flex flex-wrap items-center gap-md px-md text-sm">
          <span class="text-system-on-surface-variant font-mono text-xs truncate max-w-40">{{ ev.userId }}</span>
          <button type="button" class="text-system-primary underline underline-offset-2" @click="reveal(ev.id)">
            Révéler
          </button>
          <span v-if="revealed[ev.id]" class="font-mono">{{ revealed[ev.id] }}</span>
        </div>
      </li>
    </ul>
    <p v-else-if="!error" class="text-system-on-surface-variant text-sm">
      Aucun événement récent.
    </p>
  </div>
</template>
