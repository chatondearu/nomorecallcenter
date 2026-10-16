<script setup lang="ts">
definePageMeta({
  middleware: 'admin',
})

interface Proposal {
  id: string
  pattern: string
  matchType: string
  status: string
  proposedBy: string
  createdAt: string
}

const { apiFetch } = useApi()
const proposals = ref<Proposal[]>([])
const error = ref<string | null>(null)
const loading = ref(true)
const busyId = ref<string | null>(null)
const errorEl = ref<HTMLElement | null>(null)

watch(error, async (value) => {
  if (!value)
    return
  await nextTick()
  errorEl.value?.focus()
})

const queueSummary = computed(() => {
  const action = 'Approuver applique le motif à la liste partagée.'
  if (loading.value)
    return action
  const n = proposals.value.length
  const label = n === 1 ? 'proposition' : 'propositions'
  return `${n} ${label} en attente. ${action}`
})

async function load() {
  loading.value = true
  try {
    const res = await apiFetch<{ proposals: Proposal[] }>('/api/proposals?status=pending')
    proposals.value = res.proposals
    error.value = null
  }
  catch (e: unknown) {
    const err = e as { statusMessage?: string }
    error.value = err?.statusMessage || 'Impossible de charger la file.'
  }
  finally {
    loading.value = false
  }
}

await load()

async function moderate(id: string, action: 'approve' | 'reject') {
  if (action === 'reject' && !window.confirm('Rejeter cette proposition ? Cette action est définitive.'))
    return
  busyId.value = id
  error.value = null
  try {
    await apiFetch(`/api/proposals/${id}/${action}`, { method: 'POST' })
    proposals.value = proposals.value.filter(p => p.id !== id)
  }
  catch (e: unknown) {
    const err = e as { statusMessage?: string, data?: { message?: string } }
    const fallback = action === 'reject'
      ? 'Le rejet a échoué. Actualisez la file, puis réessayez.'
      : 'L’approbation a échoué. Actualisez la file, puis réessayez.'
    error.value = err?.data?.message || err?.statusMessage || fallback
  }
  finally {
    busyId.value = null
  }
}

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
  <div class="flex flex-col gap-lg max-w-3xl">
    <header class="flex items-start justify-between gap-md">
      <div>
        <h1 class="text-2xl font-bold text-system-on-surface">
          Modération
        </h1>
        <p class="text-system-on-surface-variant text-sm mt-xs">
          {{ queueSummary }}
        </p>
      </div>
      <FilledButton :disabled="loading" @click="load">
        Actualiser
      </FilledButton>
    </header>

    <p v-if="error" ref="errorEl" tabindex="-1" class="text-error text-sm" role="alert">
      {{ error }}
    </p>

    <p v-if="loading" class="text-system-on-surface-variant text-sm">
      Chargement…
    </p>

    <ul v-else-if="proposals.length" class="flex flex-col gap-md">
      <li
        v-for="p in proposals"
        :key="p.id"
        class="rounded-lg border border-system-outline-variant bg-system-surface-container p-md flex flex-col sm:flex-row sm:items-center gap-md justify-between"
      >
        <div>
          <p class="font-mono text-lg tabular-nums text-primary">
            {{ p.pattern }}
          </p>
          <p class="text-sm text-system-on-surface-variant mt-xs">
            {{ p.matchType }} · {{ formatTime(p.createdAt) }}
          </p>
        </div>
        <div class="flex gap-sm shrink-0">
          <FilledButton
            :disabled="busyId === p.id"
            @click="moderate(p.id, 'approve')"
          >
            Approuver
          </FilledButton>
          <!-- Shared FilledButton has no destructive style. -->
          <button
            type="button"
            class="rounded-full border border-error text-error px-lg py-sm text-sm font-medium hover:bg-system-error-container disabled:opacity-40 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-system-error"
            :disabled="busyId === p.id"
            @click="moderate(p.id, 'reject')"
          >
            Rejeter
          </button>
        </div>
      </li>
    </ul>

    <p v-else class="text-system-on-surface-variant text-sm">
      Aucune proposition en attente.
    </p>
  </div>
</template>
