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
    betterAuthSecret: process.env.BETTER_AUTH_SECRET || '',
    betterAuthUrl: process.env.BETTER_AUTH_URL || '',
    bootstrapAdminEmail: process.env.BOOTSTRAP_ADMIN_EMAIL || '',
  },
})
