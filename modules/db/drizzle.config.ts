import process from 'node:process'
import { defineConfig } from 'drizzle-kit'

export default defineConfig({
  schema: './src/schema/index.ts',
  out: './drizzle/migrations',
  dialect: 'postgresql',
  dbCredentials: {
    url: process.env.DATABASE_URL || 'postgresql://nmcc:nmcc@localhost:5432/nmcc',
  },
  verbose: true,
  strict: true,
})
