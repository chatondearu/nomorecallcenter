import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const currentDir = dirname(fileURLToPath(import.meta.url))

export default defineNuxtConfig({
  modules: [
    '@unocss/nuxt',
  ],

  // Consumers (admin, app) have no local uno.config; load the layer preset.
  unocss: {
    configFile: join(currentDir, 'uno.config.ts'),
  },

  css: [
    join(currentDir, 'app/assets/css/main.css'),
  ],

  experimental: {
    payloadExtraction: false,
    renderJsonPayloads: true,
  },

  app: {
    head: {
      htmlAttrs: { lang: 'fr' },
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      ],
    },
  },

  typescript: {
    strict: true,
  },
})
