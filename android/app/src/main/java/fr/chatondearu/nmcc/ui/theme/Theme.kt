package fr.chatondearu.nmcc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = NmccColors.primary,
    onPrimary = NmccColors.onPrimary,
    primaryContainer = NmccColors.primaryContainer,
    onPrimaryContainer = NmccColors.onPrimaryContainer,
    secondary = NmccColors.secondary,
    onSecondary = NmccColors.onSecondary,
    secondaryContainer = NmccColors.secondaryContainer,
    onSecondaryContainer = NmccColors.onSecondaryContainer,
    tertiary = NmccColors.tertiary,
    onTertiary = NmccColors.onTertiary,
    tertiaryContainer = NmccColors.tertiaryContainer,
    onTertiaryContainer = NmccColors.onTertiaryContainer,
    error = NmccColors.error,
    onError = NmccColors.onError,
    errorContainer = NmccColors.errorContainer,
    onErrorContainer = NmccColors.onErrorContainer,
    background = NmccColors.background,
    onBackground = NmccColors.onBackground,
    surface = NmccColors.surface,
    onSurface = NmccColors.onSurface,
    surfaceVariant = NmccColors.surfaceVariant,
    onSurfaceVariant = NmccColors.onSurfaceVariant,
    surfaceContainerLowest = NmccColors.surfaceContainerLowest,
    surfaceContainerLow = NmccColors.surfaceContainerLow,
    surfaceContainer = NmccColors.surfaceContainer,
    surfaceContainerHigh = NmccColors.surfaceContainerHigh,
    surfaceContainerHighest = NmccColors.surfaceContainerHighest,
    outline = NmccColors.outline,
    outlineVariant = NmccColors.outlineVariant,
)

@Composable
fun NmccTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightScheme,
        content = content,
    )
}
