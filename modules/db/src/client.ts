import process from 'node:process'
import { drizzle } from 'drizzle-orm/postgres-js'
import postgres from 'postgres'
import * as schema from './schema/index.js'

const connectionString = process.env.DATABASE_URL
  || 'postgresql://nmcc:nmcc@localhost:5432/nmcc'

const client = postgres(connectionString, { prepare: false })

export const db = drizzle(client, { schema })

export { client }
