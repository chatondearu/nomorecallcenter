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
    class="rounded-full bg-primary text-system-on-primary px-lg py-sm text-sm font-medium disabled:opacity-40"
  >
    <slot />
  </button>
</template>
