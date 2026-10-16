<script setup lang="ts">
const { user, signOut } = useAdminSession()
const route = useRoute()
const navOpen = ref(false)

watch(() => route.path, () => {
  navOpen.value = false
})

const links = [
  { to: '/', label: 'Vue d’ensemble' },
  { to: '/proposals', label: 'Modération' },
  { to: '/invites', label: 'Membres' },
  { to: '/events', label: 'Événements' },
] as const

async function onSignOut() {
  await signOut()
  await navigateTo('/login')
}
</script>

<template>
  <div class="min-h-screen flex flex-col md:flex-row bg-system-background text-system-on-background">
    <a
      href="#contenu"
      class="sr-only focus:not-sr-only focus:absolute focus:z-50 focus:bg-system-surface focus:p-sm focus:text-system-primary"
    >
      Aller au contenu
    </a>
    <button
      type="button"
      class="md:hidden m-sm w-fit rounded-lg px-md py-sm text-sm text-system-on-surface hover:bg-system-surface-container focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-system-primary"
      :aria-expanded="navOpen"
      aria-controls="admin-nav"
      @click="navOpen = !navOpen"
    >
      Menu
    </button>
    <aside
      id="admin-nav"
      class="w-full md:w-72 shrink-0 border-r border-system-outline-variant bg-system-surface-container-low p-md flex-col"
      :class="navOpen ? 'flex' : 'hidden md:flex'"
    >
      <div class="px-sm py-sm mb-md border-b border-system-outline-variant">
        <p class="text-system-on-surface font-semibold truncate" translate="no">
          nomorecallcenter
        </p>
        <p class="text-system-on-surface-variant text-sm mt-xs">
          Administration
        </p>
      </div>

      <nav class="flex flex-col gap-1 flex-1" aria-label="Administration">
        <NuxtLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="rounded-lg px-md py-sm text-sm transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-system-primary"
          :class="route.path === link.to
            ? 'bg-system-secondary-container text-system-on-secondary-container font-medium'
            : 'text-system-on-surface-variant hover:bg-system-surface-container hover:text-system-on-surface'"
          :aria-current="route.path === link.to ? 'page' : undefined"
        >
          {{ link.label }}
        </NuxtLink>
      </nav>

      <div class="border-t border-system-outline-variant pt-md text-sm">
        <p class="text-system-on-surface-variant truncate px-sm" :title="user?.email">
          {{ user?.email }}
        </p>
        <button
          type="button"
          class="mt-sm rounded-lg px-sm py-sm text-system-on-surface-variant hover:bg-system-surface-container hover:text-system-on-surface focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-system-primary"
          @click="onSignOut"
        >
          Déconnexion
        </button>
      </div>
    </aside>

    <main id="contenu" class="min-w-0 flex-1 p-lg overflow-auto bg-system-surface">
      <slot />
    </main>
  </div>
</template>
