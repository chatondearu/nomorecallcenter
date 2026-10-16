package fr.chatondearu.nmcc.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Preset id to the shared chip label (PRD-41). */
val ttlChipOptions = listOf(
    "1h" to "1h",
    "24h" to "24h",
    "7d" to "7j",
    "30d" to "30j",
    "permanent" to "Permanent",
)

fun ttlChipLabel(preset: String): String =
    ttlChipOptions.firstOrNull { it.first == preset }?.second ?: preset

@Composable
fun TtlChipRow(selected: String, onSelect: (String) -> Unit) {
    val colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        ttlChipOptions.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
                colors = colors,
            )
        }
    }
}
