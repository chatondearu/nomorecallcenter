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
      class="h-9 rounded-full px-md text-xs font-medium"
      :class="model === option
        ? 'bg-system-primary-container text-system-on-primary-container'
        : 'border border-system-outline-variant bg-system-surface-container-lowest text-system-on-surface-variant'"
      :aria-checked="model === option"
      @click="model = option"
    >
      {{ option }}
    </button>
  </div>
</template>
