/**
 * Parse ARCEP MAJNUM CSV and extract FR demarchage-style prefixes (0892*).
 * Source: Ressources en numérotation téléphonique (Licence Ouverte 2.0).
 */

export const ARCEP_MAJNUM_DEFAULT_URL = 'https://extranet.arcep.fr/uploads/MAJNUM.csv'
export const ARCEP_DATASET_PAGE = 'https://www.data.gouv.fr/datasets/ressources-en-numerotation-telephonique'
export const ARCEP_SOURCE_ID = 'arcep-majnum'
export const ARCEP_ATTRIBUTION
  = 'Derived from ARCEP MAJNUM (Ressources en numérotation téléphonique) — Licence Ouverte 2.0. '
    + `Source: ${ARCEP_DATASET_PAGE}`

/** National demarchage / telesales band in the ARCEP numbering plan. */
export const DEMARCHAGE_PREFIX = '0892'

export interface ArcepPrefixRow {
  pattern: string
}

/**
 * Parse MAJNUM CSV text (`;` separator, ANSI or UTF-8) into demarchage EZABPQM prefixes.
 */
export function parseMajnumDemarchagePrefixes(csvText: string): ArcepPrefixRow[] {
  const lines = csvText.replace(/^\uFEFF/, '').split(/\r?\n/)
  if (lines.length === 0)
    return []

  const header = lines[0]?.split(';').map(cell => cell.trim()) ?? []
  const ezIndex = header.findIndex(h => /^EZABPQM$/i.test(h))
  if (ezIndex < 0)
    throw new Error('MAJNUM CSV missing EZABPQM column')

  const seen = new Set<string>()
  const rows: ArcepPrefixRow[] = []

  for (let i = 1; i < lines.length; i++) {
    const line = lines[i]?.trim()
    if (!line)
      continue

    const cols = line.split(';')
    const raw = cols[ezIndex]?.trim() ?? ''
    // Digits only; keep allocated tranche identifiers (e.g. 089200)
    const pattern = raw.replace(/\D/g, '')
    if (!pattern.startsWith(DEMARCHAGE_PREFIX))
      continue
    if (seen.has(pattern))
      continue
    seen.add(pattern)
    rows.push({ pattern })
  }

  return rows
}
