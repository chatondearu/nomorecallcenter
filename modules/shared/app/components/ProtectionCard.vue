<script setup lang="ts">
// Fixed French labels owned by this card. Callers do not pass copy.
</script>

<template>
  <article class="flex items-center gap-sm rounded-lg bg-system-primary-container p-md text-system-on-primary-container">
    <span
      class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-system-primary text-system-on-primary"
      aria-hidden="true"
    >
      <svg viewBox="0 0 24 24" class="h-6 w-6" fill="currentColor">
        <path d="M12 2 4 5v6c0 5.25 3.4 10.15 8 11.35C16.6 21.15 20 16.25 20 11V5l-8-3z" />
      </svg>
    </span>
    <div class="flex min-w-0 flex-col items-start gap-xs">
      <h2 class="text-base font-bold tracking-wide">
        Protection active
      </h2>
      <p class="rounded-full bg-system-primary px-sm py-xs text-xs font-semibold text-system-on-primary">
        Rejet silencieux
      </p>
    </div>
  </article>
</template>
