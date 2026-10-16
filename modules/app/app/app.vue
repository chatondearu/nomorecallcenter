<script setup lang="ts">
const ttl = ref<'1h' | '24h' | '7j' | '30j' | 'Permanent'>('24h')
const effect = ref<'block' | 'allow'>('block')
const label = ref('')
</script>

<template>
  <main class="min-h-screen bg-system-background text-system-on-background p-md flex flex-col gap-lg max-w-xl">
    <header>
      <h1 class="text-primary text-2xl font-bold">
        nomorecallcenter
      </h1>
      <p class="text-system-on-surface-variant mt-sm">
        API skeleton — see <code class="font-mono">GET /api/health</code>
      </p>
    </header>

    <ProtectionCard :held="true" />
    <ProtectionCard :held="false" />

    <div class="grid grid-cols-2 gap-md">
      <StatTile label="Filtrés" :value="12" />
      <StatTile label="Bloqués" :value="3" />
    </div>

    <EventRow marker="a1b2c3d4e5…" action="blocked" time="16 oct. 17:18" />

    <TtlChips v-model="ttl" />
    <EffectChips v-model="effect" />
    <OutlinedField v-model="label" label="Libellé" />
    <FilledButton>Enregistrer</FilledButton>
  </main>
</template>
