import { join } from 'node:path'
import process from 'node:process'
import { fileURLToPath } from 'node:url'
import { drizzle } from 'drizzle-orm/postgres-js'
import { migrate } from 'drizzle-orm/postgres-js/migrator'
import postgres from 'postgres'

function requireDatabaseUrl(): string {
  const url = process.env.DATABASE_URL
  if (!url) {
    console.error('[migrate] DATABASE_URL is not set')
    process.exit(1)
  }
  return url
}

const migrationsFolder = join(
  fileURLToPath(new URL('.', import.meta.url)),
  '../drizzle/migrations',
)

async function main() {
  const url = requireDatabaseUrl()
  const safeUrl = url.replace(/:[^:@]*@/, ':****@')

  console.log(`[migrate] target=${safeUrl}`)
  console.log(`[migrate] folder=${migrationsFolder}`)

  const sql = postgres(url, { max: 1, prepare: false })
  const db = drizzle(sql)

  await migrate(db, { migrationsFolder })
  await sql.end()

  console.log('[migrate] done')
}

main().catch((error) => {
  console.error('[migrate] failed', error)
  process.exit(1)
})
