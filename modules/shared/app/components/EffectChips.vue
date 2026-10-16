<script setup lang="ts">
const options = [
  { value: 'block', label: 'Bloquer' },
  { value: 'allow', label: 'Autoriser' },
] as const

const model = defineModel<(typeof options)[number]['value']>()
</script>

<template>
  <div class="flex flex-wrap gap-sm" role="radiogroup" aria-label="Effet">
    <button
      v-for="option in options"
      :key="option.value"
      type="button"
      role="radio"
      class="rounded-full px-md py-xs text-sm border border-system-outline"
      :class="[
        option.value === 'block' ? 'text-status-blocked' : 'text-status-allowed',
        model === option.value ? 'bg-system-surface-container' : 'bg-system-surface',
      ]"
      :aria-checked="model === option.value"
      @click="model = option.value"
    >
      {{ option.label }}
    </button>
  </div>
</template>
