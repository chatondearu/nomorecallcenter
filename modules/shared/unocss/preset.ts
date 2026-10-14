import type { Preset } from 'unocss'
import tokens from '../../../design/tokens/md3-colors.json'
import { md3Theme, schemeToCssVars, systemColorTheme } from './m3'

export function nmccPreset(): Preset {
  return {
    name: 'nmcc-md3',
    theme: {
      colors: {
        ...md3Theme.palettes,
        system: systemColorTheme(md3Theme.light),
        status: {
          blocked: tokens.semantic.statusBlocked,
          allowed: tokens.semantic.statusAllowed,
          watch: tokens.semantic.statusWatch,
        },
      },
      borderRadius: tokens.radius,
      spacing: {
        ...tokens.spacing,
      },
    },
    preflights: [
      {
        getCSS: () => {
          const lightVars = Object.entries(schemeToCssVars(md3Theme.light))
            .map(([k, v]) => `${k}: ${v};`)
            .join('\n')
          const darkVars = Object.entries(schemeToCssVars(md3Theme.dark))
            .map(([k, v]) => `${k}: ${v};`)
            .join('\n')
          const semanticVars = Object.entries(tokens.semantic)
            .map(([k, v]) => `--um3-${k.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase()}: ${v};`)
            .join('\n')

          return `
:root {
${lightVars}
${semanticVars}
}
.dark {
${darkVars}
}
`
        },
      },
    ],
  }
}
