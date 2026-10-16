<script setup lang="ts">
const options = ['1h', '24h', '7j', '30j', 'Permanent'] as const

const model = defineModel<(typeof options)[number]>()
</script>

<template>
  <div class="flex flex-wrap gap-sm" role="radiogroup" aria-label="Durée">
    <button
      v-for="option in options"
      :key="option"
      type="button"
      role="radio"
      class="rounded-full px-md py-xs text-sm border border-system-outline"
      :class="model === option
        ? 'bg-primary text-system-on-primary'
        : 'bg-system-surface text-system-on-surface'"
      :aria-checked="model === option"
      @click="model = option"
    >
      {{ option }}
    </button>
  </div>
</template>
