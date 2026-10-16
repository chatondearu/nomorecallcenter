<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
}>()
</script>

<template>
  <article class="flex flex-col justify-between rounded-lg border border-system-outline-variant bg-system-surface-container p-md">
    <p class="text-xs font-medium text-system-on-surface-variant">
      {{ label }}
    </p>
    <p class="mt-sm text-2xl font-bold tabular-nums text-system-on-surface">
      {{ value }}
    </p>
  </article>
</template>
