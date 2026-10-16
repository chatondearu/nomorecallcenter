<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
}>()
</script>

<template>
  <article class="rounded-lg bg-system-surface p-md">
    <p class="text-system-on-surface-variant text-sm">
      {{ label }}
    </p>
    <p class="text-primary text-2xl font-bold tabular-nums mt-xs">
      {{ value }}
    </p>
  </article>
</template>
