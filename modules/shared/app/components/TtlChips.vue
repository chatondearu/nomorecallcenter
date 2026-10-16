<script setup lang="ts">
const options = ['1h', '24h', '7j', '30j', 'Permanent'] as const

const model = defineModel<(typeof options)[number]>()
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
  model.value = option
  focusAt(next)
}
</script>

<template>
  <div ref="group" class="flex flex-wrap gap-sm" role="radiogroup" aria-label="Durée">
    <button
      v-for="(option, index) in options"
      :key="option"
      type="button"
      role="radio"
      class="h-9 rounded-full px-md text-xs font-medium hover:border-system-primary focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-system-primary"
      :class="model === option
        ? 'bg-system-primary-container text-system-on-primary-container'
        : 'border border-system-outline-variant bg-system-surface-container-lowest text-system-on-surface-variant'"
      :aria-checked="model === option"
      :tabindex="(model ? model === option : index === 0) ? 0 : -1"
      @click="model = option"
      @keydown="onKeydown($event, index)"
    >
      {{ option }}
    </button>
  </div>
</template>
