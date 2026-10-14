const apiProxyTarget = (process.env.API_PROXY_TARGET || 'http://127.0.0.1:3000').replace(/\/$/, '')

export default defineNuxtConfig({
  extends: ['@nmcc/shared'],

  devServer: {
    port: 3001,
  },

  // Same-origin /api so session cookies stick to the admin origin (Slice 6).
  nitro: {
    routeRules: {
      '/api/**': { proxy: `${apiProxyTarget}/api/**` },
    },
  },

  runtimeConfig: {
    public: {
      // Empty = same-origin via Nitro proxy. Override only for special setups.
      apiBase: process.env.NUXT_PUBLIC_API_BASE || '',
    },
  },

  app: {
    head: {
      title: 'nomorecallcenter admin',
    },
  },
})
