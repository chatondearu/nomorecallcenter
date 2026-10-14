<script setup lang="ts">
const { user, signOut } = useAdminSession()
const route = useRoute()

const links = [
  { to: '/', label: 'Vue d’ensemble' },
  { to: '/proposals', label: 'Propositions' },
  { to: '/invites', label: 'Invitations' },
] as const

async function onSignOut() {
  await signOut()
  await navigateTo('/login')
}
</script>

<template>
  <div class="min-h-screen flex bg-system-background text-system-on-background">
    <aside class="w-56 shrink-0 border-r border-system-outline-variant bg-system-surface p-md flex flex-col gap-md">
      <div>
        <p class="text-primary text-lg font-bold tracking-tight">
          nomorecallcenter
        </p>
        <p class="text-system-on-surface-variant text-sm mt-1">
          Administration
        </p>
      </div>

      <nav class="flex flex-col gap-1 flex-1">
        <NuxtLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="rounded-md px-3 py-2 text-sm transition-colors"
          :class="route.path === link.to
            ? 'bg-primary-container text-on-primary-container font-medium'
            : 'text-system-on-surface hover:bg-system-surface-variant'"
        >
          {{ link.label }}
        </NuxtLink>
      </nav>

      <div class="border-t border-system-outline-variant pt-md text-sm">
        <p class="text-system-on-surface-variant truncate" :title="user?.email">
          {{ user?.email }}
        </p>
        <button
          type="button"
          class="mt-2 text-primary underline-offset-2 hover:underline"
          @click="onSignOut"
        >
          Déconnexion
        </button>
      </div>
    </aside>

    <main class="flex-1 p-lg overflow-auto">
      <slot />
    </main>
  </div>
</template>
