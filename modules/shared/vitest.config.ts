import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    include: ['unocss/**/*.test.ts'],
    server: {
      deps: {
        inline: ['@material/material-color-utilities'],
      },
    },
  },
})
