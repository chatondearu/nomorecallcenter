<script setup lang="ts">
definePageMeta({
  middleware: 'admin',
})

interface InstanceStats {
  proposalFunnel?: {
    submitted: number
    pending: number
    approved: number
    rejected: number
  }
  eventVolume?: number
  counters?: {
    '7d': { screened: number, blocked: number, allowed: number }
    '30d': { screened: number, blocked: number, allowed: number }
  }
}

interface FeedEvent {
  id: string
  prefix: string | null
  hash: string | null
  action: string
  userId: string
  occurredAt: string
}

const { apiFetch } = useApi()
const stats = ref<InstanceStats | null>(null)
const events = ref<FeedEvent[]>([])
const revealed = ref<Record<string, string>>({})
const error = ref<string | null>(null)
const loading = ref(true)

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
    const [s, feed] = await Promise.all([
      apiFetch<InstanceStats>('/api/stats/instance'),
      apiFetch<{ events: FeedEvent[] }>('/api/admin/events/feed?limit=40'),
    ])
    stats.value = s
    events.value = feed.events
    error.value = null
  }
  catch (e: unknown) {
    const err = e as { statusMessage?: string }
    error.value = err?.statusMessage || 'Impossible de charger les statistiques.'
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
      <h1 class="text-2xl font-bold text-system-on-background">
        Vue d’ensemble
      </h1>
      <p class="text-system-on-surface-variant text-sm mt-1">
        Entonnoir des propositions et fil d’événements (rafraîchi toutes les 5 s).
      </p>
    </header>

    <p v-if="error" class="text-error text-sm" role="alert">
      {{ error }}
    </p>
    <p v-else-if="loading" class="text-system-on-surface-variant text-sm">
      Chargement…
    </p>

    <template v-else>
      <section class="grid grid-cols-2 md:grid-cols-4 gap-md">
        <div
          v-for="tile in [
            { label: 'En attente', value: stats?.proposalFunnel?.pending ?? 0 },
            { label: 'Approuvées', value: stats?.proposalFunnel?.approved ?? 0 },
            { label: 'Rejetées', value: stats?.proposalFunnel?.rejected ?? 0 },
            { label: 'Volume événements', value: stats?.eventVolume ?? 0 },
          ]"
          :key="tile.label"
          class="rounded-lg bg-system-surface-container p-md border border-system-outline-variant"
        >
          <p class="text-system-on-surface-variant text-sm">
            {{ tile.label }}
          </p>
          <p class="text-2xl font-bold tabular-nums mt-1 text-primary">
            {{ tile.value }}
          </p>
        </div>
      </section>

      <section v-if="stats?.counters" class="text-sm text-system-on-surface-variant">
        Instance 7 j —
        filtrés {{ stats.counters['7d'].screened }},
        bloqués {{ stats.counters['7d'].blocked }},
        autorisés {{ stats.counters['7d'].allowed }}
      </section>

      <section>
        <h2 class="text-lg font-semibold mb-md">
          Fil en direct
        </h2>
        <ul v-if="events.length" class="flex flex-col gap-2">
          <li
            v-for="ev in events"
            :key="ev.id"
            class="rounded-md bg-system-surface border border-system-outline-variant px-3 py-2 text-sm flex flex-wrap gap-x-4 gap-y-1"
          >
            <span class="font-mono tabular-nums text-tertiary">{{ ev.prefix || (ev.hash ? `${ev.hash.slice(0, 10)}…` : '—') }}</span>
            <span
              :class="{
                'text-status-blocked': ev.action === 'blocked',
                'text-status-allowed': ev.action === 'allowed',
                'text-status-watch': ev.action === 'screened',
              }"
            >{{ ev.action }}</span>
            <span class="text-system-on-surface-variant">{{ formatTime(ev.occurredAt) }}</span>
            <span class="text-system-on-surface-variant font-mono text-xs truncate max-w-40">{{ ev.userId }}</span>
            <button type="button" class="text-primary underline" @click="reveal(ev.id)">
              Révéler
            </button>
            <span v-if="revealed[ev.id]" class="font-mono">{{ revealed[ev.id] }}</span>
          </li>
        </ul>
        <p v-else class="text-system-on-surface-variant text-sm">
          Aucun événement récent.
        </p>
      </section>
    </template>
  </div>
</template>
