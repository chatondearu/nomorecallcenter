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

const { apiFetch } = useApi()
const stats = ref<InstanceStats | null>(null)
const error = ref<string | null>(null)
const loading = ref(true)
const statsStatus = ref('')
let statsSignature = ''

const funnelTiles = computed(() => [
  { label: 'Propositions en attente', value: stats.value?.proposalFunnel?.pending ?? 0 },
  { label: 'Approuvées', value: stats.value?.proposalFunnel?.approved ?? 0 },
  { label: 'Rejetées', value: stats.value?.proposalFunnel?.rejected ?? 0 },
  { label: 'Volume d’événements', value: stats.value?.eventVolume ?? 0 },
])

const windowTiles = computed(() => {
  const counters = stats.value?.counters
  if (!counters)
    return []
  return [
    { label: 'Filtrés (7 j)', value: counters['7d'].screened },
    { label: 'Bloqués (7 j)', value: counters['7d'].blocked },
    { label: 'Autorisés (7 j)', value: counters['7d'].allowed },
    { label: 'Filtrés (30 j)', value: counters['30d'].screened },
    { label: 'Bloqués (30 j)', value: counters['30d'].blocked },
    { label: 'Autorisés (30 j)', value: counters['30d'].allowed },
  ]
})

async function load() {
  try {
    stats.value = await apiFetch<InstanceStats>('/api/stats/instance')
    error.value = null
    const nextSignature = JSON.stringify(stats.value)
    if (statsSignature && nextSignature !== statsSignature)
      statsStatus.value = 'Compteurs mis à jour.'
    statsSignature = nextSignature
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
</script>

<template>
  <div class="flex flex-col gap-lg max-w-5xl">
    <header>
      <h1 class="text-2xl font-bold text-system-on-surface">
        Vue d’ensemble
      </h1>
      <p class="text-system-on-surface-variant text-sm mt-xs">
        Compteurs de l’instance, rafraîchis toutes les 5 s.
      </p>
    </header>

    <p class="sr-only" aria-live="polite">
      {{ statsStatus }}
    </p>
    <p v-if="error" class="text-error text-sm" role="alert">
      {{ error }}
    </p>
    <p v-else-if="loading" class="text-system-on-surface-variant text-sm">
      Chargement…
    </p>

    <template v-else>
      <section class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-md">
        <StatTile
          v-for="tile in funnelTiles"
          :key="tile.label"
          :label="tile.label"
          :value="tile.value"
        />
      </section>

      <section v-if="windowTiles.length" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-md">
        <StatTile
          v-for="tile in windowTiles"
          :key="tile.label"
          :label="tile.label"
          :value="tile.value"
        />
      </section>

      <NuxtLink
        to="/proposals"
        class="text-sm font-medium text-system-primary hover:underline w-fit focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-system-primary"
      >
        Examiner la file
      </NuxtLink>
    </template>
  </div>
</template>
