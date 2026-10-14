import { describe, expect, it } from 'vitest'
import { DEMARCHAGE_PREFIX, parseMajnumDemarchagePrefixes } from './arcep-majnum.js'

describe('parseMajnumDemarchagePrefixes', () => {
  it('extracts unique 0892* EZABPQM prefixes', () => {
    const csv = [
      'EZABPQM;Tranche_Debut;Tranche_Fin;Mnémo;Territoire;Date_Attribution',
      '01056;0105600000;0105699999;FRTE;Métropole;01/01/2017',
      '089200;0892000000;0892009999;WRDL;National;01/07/2021',
      '089201;0892010000;0892019999;ODIG;National;01/02/2019',
      '089200;0892000000;0892009999;WRDL;National;01/07/2021',
      '089900;0899000000;0899009999;SFR0;National;01/01/2017',
    ].join('\n')

    const rows = parseMajnumDemarchagePrefixes(csv)
    expect(rows.map(r => r.pattern)).toEqual(['089200', '089201'])
    expect(rows.every(r => r.pattern.startsWith(DEMARCHAGE_PREFIX))).toBe(true)
  })

  it('throws when EZABPQM column is missing', () => {
    expect(() => parseMajnumDemarchagePrefixes('A;B\n1;2')).toThrow(/EZABPQM/)
  })
})
