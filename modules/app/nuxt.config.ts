export default defineNuxtConfig({
  extends: ['@nmcc/shared'],

  devServer: {
    port: 3000,
  },

  nitro: {
    esbuild: {
      options: {
        target: 'esnext',
      },
    },
  },

  runtimeConfig: {
    databaseUrl: process.env.DATABASE_URL || '',
  },
})
