<script setup lang="ts">
withDefaults(defineProps<{
  /** Hash or number. The caller chooses; this row does not apply storage policy. */
  title: string
  reason?: string
  time: string
  status: string
  /** ISO timestamp for the machine-readable time. Display stays in `time`. */
  dateTime?: string
}>(), {
  reason: '',
})
</script>

<template>
  <div class="flex items-center justify-between gap-sm rounded-lg border border-system-outline-variant bg-system-surface-container p-md">
    <div class="flex min-w-0 items-center gap-sm">
      <span
        class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-system-surface-container-high text-system-primary"
        aria-hidden="true"
      >
        <svg viewBox="0 0 24 24" class="h-5 w-5" fill="currentColor">
          <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1C10.6 21 3 13.4 3 4c0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.4 0 .7-.3 1L6.6 10.8z" />
        </svg>
      </span>
      <div class="min-w-0">
        <p class="truncate font-mono text-sm font-semibold text-system-on-surface">
          {{ title }}
        </p>
        <div class="mt-xs flex min-w-0 items-center gap-sm">
          <span class="shrink-0 rounded-full bg-system-secondary-container px-sm py-xs text-xs font-medium text-system-on-secondary-container">
            {{ status }}
          </span>
          <span v-if="reason" class="truncate text-sm text-system-on-surface-variant">
            {{ reason }}
          </span>
        </div>
      </div>
    </div>
    <time :datetime="dateTime" class="shrink-0 font-mono text-sm text-system-on-surface-variant">{{ time }}</time>
  </div>
</template>
