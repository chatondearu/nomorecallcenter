import type { TonalPalette } from '@material/material-color-utilities'
import {
  argbFromHex,
  DynamicColor,
  Hct,
  hexFromArgb,
  MaterialDynamicColors,
  SchemeTonalSpot,
} from '@material/material-color-utilities'
import tokens from '../../../design/tokens/md3-colors.json'

type UnoPalette = Record<string, string> & { DEFAULT: string }

export interface Md3Theme {
  seed: string
  light: Record<string, number>
  dark: Record<string, number>
  palettes: Record<string, UnoPalette>
  semantic: Record<string, string>
}

const colorRoles = Object.keys(MaterialDynamicColors).filter((key) => {
  const color = MaterialDynamicColors[key as keyof typeof MaterialDynamicColors]
  return color instanceof DynamicColor
})

function schemeColors(scheme: SchemeTonalSpot): Record<string, number> {
  return Object.fromEntries(
    colorRoles
      .map((role) => {
        const color = MaterialDynamicColors[role as keyof typeof MaterialDynamicColors]
        if (color instanceof DynamicColor)
          return [role, color.getArgb(scheme)] as const
        return null
      })
      .filter((entry): entry is readonly [string, number] => entry !== null),
  )
}

function tonalScaleToUnoPalette(scale: TonalPalette): UnoPalette {
  const tones = [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 98, 99, 100]
  const hexs = Object.fromEntries(
    tones.map(tone => [tone, hexFromArgb(scale.tone(tone))]),
  ) as Record<string, string>

  return {
    DEFAULT: hexs[40],
    ...hexs,
  }
}

function toKebab(token: string) {
  return token.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase()
}

export function schemeToCssVars(
  scheme: Record<string, number>,
  prefix = 'um3',
): Record<string, string> {
  return Object.fromEntries(
    Object.entries(scheme).map(([key, value]) => [
      `--${prefix}-${toKebab(key)}`,
      hexFromArgb(value),
    ]),
  )
}

export function themeFromSeed(seed = tokens.seed): Md3Theme {
  const source = argbFromHex(seed)
  const hct = Hct.fromInt(source)
  const light = new SchemeTonalSpot(hct, false, 0)
  const dark = new SchemeTonalSpot(hct, true, 0)

  return {
    seed,
    light: schemeColors(light),
    dark: schemeColors(dark),
    palettes: {
      primary: tonalScaleToUnoPalette(light.primaryPalette),
      secondary: tonalScaleToUnoPalette(light.secondaryPalette),
      tertiary: tonalScaleToUnoPalette(light.tertiaryPalette),
      error: tonalScaleToUnoPalette(light.errorPalette),
      neutral: tonalScaleToUnoPalette(light.neutralPalette),
    },
    semantic: { ...tokens.semantic },
  }
}

export const md3Theme = themeFromSeed(tokens.seed)

export function systemColorTheme(scheme: Record<string, number>) {
  return Object.fromEntries(
    Object.keys(scheme).map((key) => {
      const kebab = toKebab(key)
      return [kebab.includes('-') ? kebab : key, `var(--um3-${kebab})`]
    }),
  )
}
