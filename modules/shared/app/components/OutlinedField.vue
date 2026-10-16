<script setup lang="ts">
defineProps<{
  label: string
}>()

const model = defineModel<string>({ required: true })

const id = useId()

function onInput(event: Event) {
  const target = event.target
  if (target instanceof HTMLInputElement)
    model.value = target.value
}
</script>

<template>
  <div class="flex flex-col gap-xs">
    <label :for="id" class="text-xs font-semibold text-system-on-surface">
      {{ label }}
    </label>
    <input
      :id="id"
      :value="model"
      class="rounded-lg border border-system-outline-variant bg-system-surface-container-low px-md py-sm text-sm text-system-on-surface outline-none focus:border-system-primary"
      @input="onInput"
    >
  </div>
</template>
