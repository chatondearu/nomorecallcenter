<script setup lang="ts">
const options = [
  { value: 'block', label: 'Bloquer' },
  { value: 'allow', label: 'Autoriser' },
] as const

const model = defineModel<(typeof options)[number]['value']>()
const group = ref<HTMLElement | null>(null)

function focusAt(index: number) {
  const buttons = group.value?.querySelectorAll<HTMLButtonElement>('[role="radio"]')
  buttons?.[index]?.focus()
}

function onKeydown(event: KeyboardEvent, index: number) {
  const delta = event.key === 'ArrowRight' || event.key === 'ArrowDown'
    ? 1
    : event.key === 'ArrowLeft' || event.key === 'ArrowUp'
      ? -1
      : 0
  if (!delta)
    return
  event.preventDefault()
  const next = (index + delta + options.length) % options.length
  const option = options[next]
  if (!option)
    return
  model.value = option.value
  focusAt(next)
}
</script>

<template>
  <div ref="group" class="flex flex-wrap gap-sm" role="radiogroup" aria-label="Effet">
    <button
      v-for="(option, index) in options"
      :key="option.value"
      type="button"
      role="radio"
      class="rounded-full px-md py-xs text-sm border border-system-outline hover:bg-system-surface-container focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-system-primary"
      :class="[
        option.value === 'block' ? 'text-status-blocked' : 'text-status-allowed',
        model === option.value ? 'bg-system-surface-container' : 'bg-system-surface',
      ]"
      :aria-checked="model === option.value"
      :tabindex="(model ? model === option.value : index === 0) ? 0 : -1"
      @click="model = option.value"
      @keydown="onKeydown($event, index)"
    >
      {{ option.label }}
    </button>
  </div>
</template>
