import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const currentDir = dirname(fileURLToPath(import.meta.url))

export default defineNuxtConfig({
  modules: [
    '@unocss/nuxt',
  ],

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
