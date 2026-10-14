<script setup lang="ts">
definePageMeta({
  middleware: 'admin',
})

const { apiFetch } = useApi()
const email = ref('')
const expiresInDays = ref(7)
const pending = ref(false)
const error = ref<string | null>(null)
const created = ref<{ token: string, url: string, email: string | null } | null>(null)

async function onSubmit() {
  pending.value = true
  error.value = null
  created.value = null
  try {
    const body: { email?: string, expiresInDays: number } = {
      expiresInDays: expiresInDays.value,
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
    error.value = err?.data?.message || err?.statusMessage || 'Échec de création.'
  }
  finally {
    pending.value = false
  }
}
</script>

<template>
  <div class="flex flex-col gap-lg max-w-lg">
    <header>
      <h1 class="text-2xl font-bold">
        Invitations
      </h1>
      <p class="text-system-on-surface-variant text-sm mt-1">
        Créer un jeton d’invitation (inscription réservée aux invités).
      </p>
    </header>

    <form
      class="rounded-lg border border-system-outline-variant bg-system-surface p-md flex flex-col gap-md"
      @submit.prevent="onSubmit"
    >
      <label class="flex flex-col gap-1 text-sm">
        <span class="text-system-on-surface-variant">E-mail (optionnel)</span>
        <input
          v-model="email"
          type="email"
          class="rounded-md border border-system-outline bg-system-surface-container px-3 py-2"
          placeholder="membre@exemple.fr"
        >
      </label>

      <label class="flex flex-col gap-1 text-sm">
        <span class="text-system-on-surface-variant">Expire dans (jours)</span>
        <input
          v-model.number="expiresInDays"
          type="number"
          min="1"
          max="365"
          required
          class="rounded-md border border-system-outline bg-system-surface-container px-3 py-2 tabular-nums"
        >
      </label>

      <p v-if="error" class="text-error text-sm" role="alert">
        {{ error }}
      </p>

      <button
        type="submit"
        class="rounded-md bg-primary text-on-primary px-4 py-2 font-medium disabled:opacity-50 self-start"
        :disabled="pending"
      >
        {{ pending ? 'Création…' : 'Créer une invitation' }}
      </button>
    </form>

    <div
      v-if="created"
      class="rounded-lg border border-primary-container bg-primary-container/20 p-md text-sm flex flex-col gap-2"
    >
      <p class="font-medium text-primary">
        Invitation créée
      </p>
      <p v-if="created.email" class="text-system-on-surface-variant">
        Destinée à {{ created.email }}
      </p>
      <label class="flex flex-col gap-1">
        <span class="text-system-on-surface-variant">Jeton</span>
        <code class="font-mono break-all bg-system-surface-container px-2 py-1 rounded-md">{{ created.token }}</code>
      </label>
    </div>
  </div>
</template>
