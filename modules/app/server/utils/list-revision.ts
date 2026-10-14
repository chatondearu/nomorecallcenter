import { LISTS_REVISION_KEY, db, listEntries, schemaMeta } from '@nmcc/db'
import { eq } from 'drizzle-orm'

const PROPOSAL_SOURCE = 'proposal'
const PROPOSAL_ATTRIBUTION = 'Member proposal approved by instance admin (ADR-001).'

/**
 * Upsert a shared list entry from an approved proposal and bump sync revision.
 * Returns the new revision.
 */
export async function applyProposalToSharedList(pattern: string): Promise<number> {
  const now = new Date()
  const sourceDate = now.toISOString().slice(0, 10)

  const [meta] = await db
    .select()
    .from(schemaMeta)
    .where(eq(schemaMeta.key, LISTS_REVISION_KEY))
    .limit(1)

  const nextRevision = (meta ? Number(meta.value) : 0) + 1

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

  await db
    .insert(listEntries)
    .values({
      pattern,
      source: PROPOSAL_SOURCE,
      attribution: PROPOSAL_ATTRIBUTION,
      sourceDate,
      active: true,
      revision: nextRevision,
      createdAt: now,
      updatedAt: now,
    })
    .onConflictDoUpdate({
      target: listEntries.pattern,
      set: {
        source: PROPOSAL_SOURCE,
        attribution: PROPOSAL_ATTRIBUTION,
        sourceDate,
        active: true,
        revision: nextRevision,
        updatedAt: now,
      },
    })

  return nextRevision
}
