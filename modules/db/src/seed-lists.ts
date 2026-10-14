import process from 'node:process'
import { and, eq, inArray, not } from 'drizzle-orm'
import { drizzle } from 'drizzle-orm/postgres-js'
import postgres from 'postgres'
import {
  ARCEP_ATTRIBUTION,
  ARCEP_MAJNUM_DEFAULT_URL,
  ARCEP_SOURCE_ID,
  DEMARCHAGE_PREFIX,
  parseMajnumDemarchagePrefixes,
} from './arcep-majnum.js'
import { LISTS_REVISION_KEY, listEntries, schemaMeta } from './schema/index.js'

function requireDatabaseUrl(): string {
  const url = process.env.DATABASE_URL
  if (!url) {
    console.error('[lists:seed] DATABASE_URL is not set')
    process.exit(1)
  }
  return url
}

async function fetchMajnumCsv(url: string): Promise<string> {
  const response = await fetch(url)
  if (!response.ok)
    throw new Error(`Failed to fetch MAJNUM (${response.status} ${response.statusText}) from ${url}`)

  const buffer = Buffer.from(await response.arrayBuffer())
  // ARCEP files are typically Windows-1252 / Latin-1
  try {
    return new TextDecoder('windows-1252').decode(buffer)
  }
  catch {
    return buffer.toString('utf8')
  }
}

/** Offline/CI fallback: one known demarchage-style prefix with LO 2.0 attribution (not a third-party dump). */
function fixturePrefixes() {
  return [{ pattern: DEMARCHAGE_PREFIX }]
}

async function main() {
  const url = requireDatabaseUrl()
  const majnumUrl = process.env.ARCEP_MAJNUM_URL || ARCEP_MAJNUM_DEFAULT_URL
  const useFixture = process.argv.includes('--fixture')
    || process.env.ARCEP_SEED_FIXTURE === '1'

  const sourceDate = new Date().toISOString().slice(0, 10)
  let prefixes: { pattern: string }[]
  let sourceNote = majnumUrl

  if (useFixture) {
    prefixes = fixturePrefixes()
    sourceNote = 'fixture'
    console.log(`[lists:seed] fixture mode — seeding ${DEMARCHAGE_PREFIX}`)
  }
  else {
    console.log(`[lists:seed] fetching ${majnumUrl}`)
    const csv = await fetchMajnumCsv(majnumUrl)
    prefixes = parseMajnumDemarchagePrefixes(csv)
    if (prefixes.length === 0) {
      console.error('[lists:seed] no 0892* prefixes found in MAJNUM')
      process.exit(1)
    }
    console.log(`[lists:seed] parsed ${prefixes.length} demarchage prefixes`)
  }

  const sqlClient = postgres(url, { max: 1, prepare: false })
  const db = drizzle(sqlClient)

  try {
    const [meta] = await db
      .select()
      .from(schemaMeta)
      .where(eq(schemaMeta.key, LISTS_REVISION_KEY))
      .limit(1)

    const nextRevision = (meta ? Number(meta.value) : 0) + 1
    const patterns = prefixes.map(p => p.pattern)
    const now = new Date()

    await db
      .insert(schemaMeta)
      .values({
        key: LISTS_REVISION_KEY,
        value: String(nextRevision),
        updatedAt: now,
      })
      .onConflictDoUpdate({
        target: schemaMeta.key,
        set: {
          value: String(nextRevision),
          updatedAt: now,
        },
      })

    for (const pattern of patterns) {
      await db
        .insert(listEntries)
        .values({
          pattern,
          source: ARCEP_SOURCE_ID,
          attribution: ARCEP_ATTRIBUTION,
          sourceDate,
          active: true,
          revision: nextRevision,
          createdAt: now,
          updatedAt: now,
        })
        .onConflictDoUpdate({
          target: listEntries.pattern,
          set: {
            source: ARCEP_SOURCE_ID,
            attribution: ARCEP_ATTRIBUTION,
            sourceDate,
            active: true,
            revision: nextRevision,
            updatedAt: now,
          },
        })
    }

    const tombstone = await db
      .update(listEntries)
      .set({
        active: false,
        revision: nextRevision,
        updatedAt: now,
      })
      .where(and(
        eq(listEntries.source, ARCEP_SOURCE_ID),
        eq(listEntries.active, true),
        not(inArray(listEntries.pattern, patterns)),
      ))
      .returning({ pattern: listEntries.pattern })

    console.log(`[lists:seed] revision=${nextRevision} active=${patterns.length} source=${sourceNote}`)
    if (tombstone.length > 0)
      console.log(`[lists:seed] tombstoned ${tombstone.length} stale entries`)
  }
  finally {
    await sqlClient.end()
  }
}

main().catch((error) => {
  console.error('[lists:seed] failed', error)
  process.exit(1)
})
