<script setup lang="ts">
definePageMeta({
  layout: 'auth',
})

const route = useRoute()
const { signIn, refresh, user } = useAdminSession()

const email = ref('')
const password = ref('')
const error = ref<string | null>(null)
const pending = ref(false)

onMounted(async () => {
  if (route.query.error === 'admin')
    error.value = 'Un compte administrateur est requis.'
  await refresh()
  if (user.value?.role === 'admin') {
    await navigateTo((route.query.redirect as string) || '/')
  }
})

async function onSubmit() {
  error.value = null
  pending.value = true
  try {
    const sessionUser = await signIn(email.value.trim(), password.value)
    if (!sessionUser || sessionUser.role !== 'admin') {
      error.value = 'Un compte administrateur est requis.'
      return
    }
    await navigateTo((route.query.redirect as string) || '/')
  }
  catch (e: unknown) {
    const err = e as { data?: { message?: string }, statusMessage?: string }
    error.value = err?.data?.message || err?.statusMessage || 'Échec de la connexion.'
  }
  finally {
    pending.value = false
  }
}
</script>

<template>
  <form
    class="bg-system-surface rounded-lg p-lg border border-system-outline-variant flex flex-col gap-md"
    @submit.prevent="onSubmit"
  >
    <div>
      <h1 class="text-primary text-2xl font-bold">
        nomorecallcenter
      </h1>
      <p class="text-system-on-surface-variant mt-1 text-sm">
        Connexion administrateur
      </p>
    </div>

    <label class="flex flex-col gap-1 text-sm">
      <span class="text-system-on-surface-variant">E-mail</span>
      <input
        v-model="email"
        type="email"
        required
        autocomplete="username"
        class="rounded-md border border-system-outline bg-system-surface-container px-3 py-2 text-system-on-surface"
      >
    </label>

    <label class="flex flex-col gap-1 text-sm">
      <span class="text-system-on-surface-variant">Mot de passe</span>
      <input
        v-model="password"
        type="password"
        required
        autocomplete="current-password"
        class="rounded-md border border-system-outline bg-system-surface-container px-3 py-2 text-system-on-surface"
      >
    </label>

    <p v-if="error" class="text-error text-sm" role="alert">
      {{ error }}
    </p>

    <button
      type="submit"
      class="rounded-md bg-primary text-on-primary px-4 py-2 font-medium disabled:opacity-50"
      :disabled="pending"
    >
      {{ pending ? 'Connexion…' : 'Se connecter' }}
    </button>
  </form>
</template>
