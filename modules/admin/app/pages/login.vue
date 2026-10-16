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
const passwordId = useId()

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
  if (!email.value.trim() || !password.value) {
    error.value = 'E-mail et mot de passe sont requis.'
    return
  }
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
    class="bg-system-surface rounded-lg p-lg flex flex-col gap-md"
    @submit.prevent="onSubmit"
  >
    <div>
      <h1 class="text-primary text-2xl font-bold">
        nomorecallcenter
      </h1>
      <p class="text-system-on-surface-variant mt-xs text-sm">
        Connexion administrateur
      </p>
    </div>

    <OutlinedField v-model="email" label="E-mail" />

    <!-- OutlinedField cannot set input type="password". -->
    <div class="flex flex-col gap-xs">
      <label :for="passwordId" class="text-sm text-system-on-surface">
        Mot de passe
      </label>
      <input
        :id="passwordId"
        v-model="password"
        type="password"
        required
        autocomplete="current-password"
        class="rounded-sm border border-system-outline bg-system-surface px-md py-sm text-system-on-surface outline-none focus:border-primary"
      >
    </div>

    <p v-if="error" class="text-error text-sm" role="alert">
      {{ error }}
    </p>

    <FilledButton type="submit" :disabled="pending">
      {{ pending ? 'Connexion…' : 'Se connecter' }}
    </FilledButton>
  </form>
</template>
