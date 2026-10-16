<script setup lang="ts">
withDefaults(defineProps<{
  label: string
  type?: 'text' | 'email' | 'url' | 'number' | 'password'
  name?: string
  autocomplete?: string
  inputmode?: 'text' | 'numeric' | 'email' | 'url' | 'decimal'
  spellcheck?: boolean
}>(), {
  type: 'text',
})

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
      :type="type"
      :name="name"
      :autocomplete="autocomplete"
      :inputmode="inputmode"
      :spellcheck="spellcheck"
      class="rounded-lg border border-system-outline-variant bg-system-surface-container-low px-md py-sm text-sm text-system-on-surface focus-visible:border-system-primary focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-system-primary"
      @input="onInput"
    >
  </div>
</template>
