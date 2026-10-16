<script setup lang="ts">
defineProps<{
  /** Call-screening role is held on this device. */
  held: boolean
}>()
</script>

<template>
  <article class="rounded-lg bg-system-surface p-md flex flex-col gap-xs">
    <h2 class="text-system-on-surface text-lg font-medium">
      Protection
    </h2>
    <p :class="held ? 'text-primary' : 'text-system-on-surface-variant'">
      {{ held ? 'Rôle accordé' : 'Rôle non accordé' }}
    </p>
  </article>
</template>
