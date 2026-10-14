import {
  defineConfig,
  presetUno,
  presetWebFonts,
  transformerDirectives,
  transformerVariantGroup,
} from 'unocss'
import { nmccPreset } from './unocss/preset'

export default defineConfig({
  presets: [
    presetUno(),
    presetWebFonts({
      provider: 'google',
      fonts: {
        sans: 'Roboto Flex',
        mono: 'Roboto Mono',
      },
    }),
    nmccPreset(),
  ],
  transformers: [transformerDirectives(), transformerVariantGroup()],
})
