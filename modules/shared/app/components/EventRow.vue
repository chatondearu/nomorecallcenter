<script setup lang="ts">
defineProps<{
  /** Hash or prefix. Never a clear phone number. */
  marker: string
  action: string
  time: string
}>()
</script>

<template>
  <div class="flex flex-wrap items-baseline gap-x-md gap-y-xs rounded-md bg-system-surface px-md py-sm text-sm">
    <span class="font-mono text-system-on-surface">{{ marker }}</span>
    <span
      :class="{
        'text-status-blocked': action === 'blocked',
        'text-status-allowed': action === 'allowed',
        'text-status-watch': action !== 'blocked' && action !== 'allowed',
      }"
    >{{ action }}</span>
    <time class="text-system-on-surface-variant">{{ time }}</time>
  </div>
</template>
