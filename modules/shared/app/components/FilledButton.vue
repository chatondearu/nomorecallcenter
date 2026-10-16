<script setup lang="ts">
withDefaults(defineProps<{
  type?: 'button' | 'submit'
  disabled?: boolean
}>(), {
  type: 'button',
  disabled: false,
})
</script>

<template>
  <button
    :type="type"
    :disabled="disabled"
    class="inline-flex h-12 items-center justify-center rounded-full bg-system-primary-container px-lg text-sm font-semibold text-system-on-primary disabled:opacity-40"
  >
    <slot />
  </button>
</template>
