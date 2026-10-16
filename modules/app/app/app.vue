<script setup lang="ts">
// Markdown docs are not Nuxt routes; link the repo anchors that already exist.
const selfHostDoc = 'https://github.com/chatondearu/nomorecallcenter/blob/main/doc/deployment.md#full-self-host-smoke'
const fdroidDoc = 'https://github.com/chatondearu/nomorecallcenter/blob/main/android/README.md#f-droid-oriented-notes'

const ctas = [
  { href: selfHostDoc, label: 'Héberger', primary: true },
  { href: fdroidDoc, label: 'Notes F-Droid', primary: false },
] as const

const steps = [
  {
    title: 'Héberger le serveur',
    body: 'Postgres, l’image OCI et Compose. La marche à suivre est dans la documentation d’installation.',
  },
  {
    title: 'Inviter les personnes',
    body: 'Le premier compte administre l’instance. Les suivants arrivent par une invitation.',
  },
  {
    title: 'Filtrer les appels',
    body: 'L’accueil indique la protection seulement quand le rôle de filtrage d’appels est accordé.',
  },
] as const

const instanceUrl = ref(useRequestURL().origin)
const copyState = ref<'idle' | 'copied' | 'failed'>('idle')

const linkClass = {
  primary: 'inline-flex items-center justify-center rounded-full bg-primary text-system-on-primary px-lg py-sm text-sm font-medium no-underline whitespace-nowrap active:opacity-80 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2',
  secondary: 'inline-flex items-center justify-center rounded-full border border-system-outline bg-system-surface text-primary px-lg py-sm text-sm font-medium no-underline whitespace-nowrap active:opacity-80 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2',
} as const

useHead({
  title: 'nomorecallcenter',
  meta: [
    {
      name: 'description',
      content: 'Filtre d’appels Android à héberger soi-même. Accès sur invitation. Notes F-Droid.',
    },
  ],
})

watch(instanceUrl, () => {
  copyState.value = 'idle'
})

async function copyInstanceUrl() {
  try {
    await navigator.clipboard.writeText(instanceUrl.value)
    copyState.value = 'copied'
  }
  catch {
    copyState.value = 'failed'
  }
}
</script>

<template>
  <div class="min-h-dvh bg-system-background text-system-on-background">
    <a
      href="#contenu"
      class="sr-only focus:not-sr-only focus:absolute focus:z-50 focus:bg-system-surface focus:p-sm focus:text-primary"
    >
      Aller au contenu
    </a>
    <header class="mx-auto flex max-w-5xl flex-col gap-sm px-md py-md md:min-h-16 md:flex-row md:items-center md:justify-between">
      <p class="text-lg font-medium text-primary" translate="no">
        nomorecallcenter
      </p>
      <nav class="flex flex-wrap items-center gap-md md:flex-nowrap" aria-label="Documentation">
        <a
          v-for="cta in ctas"
          :key="cta.href"
          :href="cta.href"
          class="text-sm font-medium text-primary underline-offset-2 hover:underline whitespace-nowrap"
          :aria-label="`${cta.label}, nouvelle fenêtre`"
          target="_blank"
          rel="noreferrer"
        >
          {{ cta.label }}
        </a>
      </nav>
    </header>

    <main id="contenu" class="mx-auto flex max-w-5xl flex-col gap-xl px-md pb-xl">
      <section class="grid grid-cols-1 items-center gap-lg md:grid-cols-2">
        <div class="flex flex-col gap-md">
          <h1 class="text-balance text-4xl font-medium leading-tight text-system-on-background md:text-5xl">
            Un téléphone silencieux, un contrôle limpide.
          </h1>
          <p class="max-w-prose text-base leading-relaxed text-system-on-surface-variant">
            Un client Android et un serveur Postgres que vous hébergez. L’accès se fait sur invitation.
          </p>
          <div class="flex flex-wrap items-center gap-sm">
            <a
              v-for="cta in ctas"
              :key="cta.href"
              :href="cta.href"
              :class="cta.primary ? linkClass.primary : linkClass.secondary"
              :aria-label="`${cta.label}, nouvelle fenêtre`"
              target="_blank"
              rel="noreferrer"
            >
              {{ cta.label }}
            </a>
          </div>
        </div>

        <!-- Phone frame only; the call itself is the shared activity row. -->
        <figure class="mx-auto w-full max-w-sm rounded-3xl border border-system-outline-variant bg-system-surface-container-high p-sm">
          <div class="flex flex-col gap-md rounded-2xl border border-system-outline-variant bg-system-surface p-md">
            <p class="font-mono text-xs text-system-on-surface-variant">
              14:02
            </p>
            <ProtectionCard />
            <EventRow
              title="+33 1 62 •• ••"
              reason="Démarchage"
              status="Silencieux"
              time="14:02"
            />
          </div>
          <figcaption class="sr-only">
            Exemple d’un appel rejeté en silence
          </figcaption>
        </figure>
      </section>

      <section aria-labelledby="steps-title">
        <h2 id="steps-title" class="text-2xl font-medium text-system-on-background">
          Mise en route
        </h2>
        <ol class="mt-md grid list-none grid-cols-1 gap-md md:grid-cols-3">
          <li
            v-for="(step, index) in steps"
            :key="step.title"
            class="flex flex-col gap-sm rounded-lg border border-system-outline-variant bg-system-surface-container-low p-md"
          >
            <span
              class="flex h-10 w-10 items-center justify-center rounded-full bg-system-secondary-container text-sm font-medium text-system-primary"
              aria-hidden="true"
            >
              {{ index + 1 }}
            </span>
            <h3 class="text-base font-medium text-primary">
              {{ step.title }}
            </h3>
            <p class="text-system-on-surface-variant leading-relaxed">
              {{ step.body }}
            </p>
          </li>
        </ol>
      </section>

      <section class="flex flex-col gap-md" aria-labelledby="instance-title">
        <h2 id="instance-title" class="text-2xl font-medium text-system-on-background">
          Adresse de l’instance
        </h2>
        <div class="grid grid-cols-1 items-start gap-lg md:grid-cols-2">
          <form class="flex flex-col gap-sm" @submit.prevent="copyInstanceUrl">
            <OutlinedField
              v-model="instanceUrl"
              label="URL de l’API"
              type="url"
              name="instance-url"
              autocomplete="url"
              :spellcheck="false"
            />
            <p class="text-sm text-system-on-surface-variant">
              Collez-la dans l’application Android. Aucun compte n’est créé ici.
            </p>
            <div>
              <FilledButton type="submit" :disabled="instanceUrl.trim().length === 0">
                {{ copyState === 'copied' ? 'Copié' : 'Copier' }}
              </FilledButton>
              <p class="sr-only" aria-live="polite">
                {{ copyState === 'copied' ? 'Adresse copiée.' : '' }}
              </p>
            </div>
            <p v-if="copyState === 'failed'" class="text-sm text-error" role="alert">
              La copie a échoué. Sélectionnez l’adresse à la main.
            </p>
          </form>
          <div class="grid grid-cols-2 gap-md">
            <StatTile label="Serveur" value="Postgres" />
            <StatTile label="Accès" value="Invitation" />
          </div>
        </div>
      </section>
    </main>

    <footer class="mx-auto max-w-5xl px-md pb-lg text-sm text-system-on-surface-variant">
      <a
        class="text-primary underline-offset-2 hover:underline"
        href="https://github.com/chatondearu/nomorecallcenter/blob/main/LICENSE"
        target="_blank"
        rel="noreferrer"
      >
        AGPL-3.0
      </a>
    </footer>
  </div>
</template>
