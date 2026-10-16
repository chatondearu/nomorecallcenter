<script setup lang="ts">
definePageMeta({
  middleware: 'admin',
})

const { apiFetch } = useApi()
const email = ref('')
const expiresInDays = ref('7')
const pending = ref(false)
const error = ref<string | null>(null)
const created = ref<{ token: string, url: string, email: string | null } | null>(null)
const errorEl = ref<HTMLElement | null>(null)

watch(error, async (value) => {
  if (!value)
    return
  await nextTick()
  errorEl.value?.focus()
})

function parseDays(raw: string) {
  const days = Number(raw)
  if (!Number.isInteger(days) || days < 1 || days > 365)
    return null
  return days
}

async function onSubmit() {
  pending.value = true
  error.value = null
  created.value = null
  const days = parseDays(expiresInDays.value.trim())
  if (days === null) {
    error.value = 'La durée doit être un entier entre 1 et 365.'
    pending.value = false
    return
  }
  try {
    const body: { email?: string, expiresInDays: number } = {
      expiresInDays: days,
    }
    const trimmed = email.value.trim()
    if (trimmed)
      body.email = trimmed

    const res = await apiFetch<{
      token: string
      url: string
      invite: { email: string | null }
    }>('/api/invites', {
      method: 'POST',
      body,
    })
    created.value = {
      token: res.token,
      url: res.url,
      email: res.invite.email,
    }
    email.value = ''
  }
  catch (e: unknown) {
    const err = e as { statusMessage?: string, data?: { message?: string } }
    error.value = err?.data?.message || err?.statusMessage || 'Échec de création. Vérifiez les champs, puis réessayez.'
  }
  finally {
    pending.value = false
  }
}
</script>

<template>
  <div class="flex flex-col gap-lg max-w-lg">
    <header>
      <h1 class="text-2xl font-bold text-system-on-surface">
        Membres
      </h1>
      <p class="text-system-on-surface-variant text-sm mt-xs">
        Créer un jeton d’invitation (inscription réservée aux invités).
      </p>
    </header>

    <form
      class="rounded-lg border border-system-outline-variant bg-system-surface-container p-md flex flex-col gap-md"
      @submit.prevent="onSubmit"
    >
      <OutlinedField
        v-model="email"
        label="E-mail (optionnel)"
        type="email"
        name="email"
        autocomplete="email"
        :spellcheck="false"
      />
      <OutlinedField
        v-model="expiresInDays"
        label="Expire dans (jours)"
        type="number"
        name="expiresInDays"
        inputmode="numeric"
        autocomplete="off"
      />

      <p v-if="error" ref="errorEl" tabindex="-1" class="text-error text-sm" role="alert">
        {{ error }}
      </p>

      <FilledButton type="submit" :disabled="pending">
        {{ pending ? 'Création…' : 'Créer une invitation' }}
      </FilledButton>
    </form>

    <div
      v-if="created"
      class="rounded-lg bg-system-primary-container p-md text-sm flex flex-col gap-sm"
      role="status"
      aria-live="polite"
    >
      <p class="font-medium text-system-on-primary-container">
        Invitation créée
      </p>
      <p v-if="created.email" class="text-system-on-primary-container">
        Destinée à {{ created.email }}
      </p>
      <label class="flex flex-col gap-xs">
        <span class="text-system-on-primary-container">Jeton</span>
        <code class="font-mono break-all bg-system-surface text-system-on-surface px-sm py-xs rounded-md">{{ created.token }}</code>
      </label>
    </div>
  </div>
</template>
