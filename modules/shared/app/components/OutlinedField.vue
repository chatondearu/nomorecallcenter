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
    <label :for="id" class="text-sm text-system-on-surface">
      {{ label }}
    </label>
    <input
      :id="id"
      :value="model"
      class="rounded-sm border border-system-outline bg-system-surface px-md py-sm text-system-on-surface outline-none focus:border-primary"
      @input="onInput"
    >
  </div>
</template>
