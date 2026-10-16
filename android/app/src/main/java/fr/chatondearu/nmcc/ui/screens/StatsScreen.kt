package fr.chatondearu.nmcc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.chatondearu.nmcc.NmccApp
import fr.chatondearu.nmcc.network.StatsBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

private fun actionLabel(action: String): String = when (action) {
    "blocked" -> "bloqué"
    "allowed" -> "autorisé"
    else -> action
}

@Composable
private fun StatTileRow(tiles: List<Pair<String, Int>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tiles.forEach { (label, value) ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(value.toString(), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun StatsScreen() {
    val app = NmccApp.instance
    var scope by remember { mutableStateOf("local") }
    var stats by remember { mutableStateOf<StatsBundle?>(null) }
    var localLines by remember { mutableStateOf<List<String>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(scope) {
        error = null
        stats = null
        localLines = emptyList()
        if (scope == "local") {
            withContext(Dispatchers.IO) {
                val queue = JSONArray(app.prefs.getEventQueueJson())
                localLines = buildList {
                    for (i in (queue.length() - 1) downTo 0) {
                        val o = queue.getJSONObject(i)
                        add(
                            "${o.optString("occurredAt")} · ${actionLabel(o.optString("action"))} · " +
                                (o.optString("prefix").ifBlank { "—" }),
                        )
                    }
                }
            }
            return@LaunchedEffect
        }
        runCatching {
            withContext(Dispatchers.IO) {
                val base = app.prefs.getBaseUrl()
                require(base.isNotBlank()) { "Indiquez l’URL de l’instance dans Paramètres" }
                if (scope == "personal") app.api.statsMe(base) else app.api.statsInstance(base)
            }
        }.onSuccess { stats = it }
            .onFailure { error = it.message }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Statistiques", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = scope == "local",
                onClick = { scope = "local" },
                label = { Text("File locale") },
            )
            FilterChip(
                selected = scope == "personal",
                onClick = { scope = "personal" },
                label = { Text("Personnel") },
            )
            FilterChip(
                selected = scope == "instance",
                onClick = { scope = "instance" },
                label = { Text("Instance") },
            )
        }

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        if (scope == "local") {
            Text("Événements locaux en attente ou récents", style = MaterialTheme.typography.titleMedium)
            if (localLines.isEmpty()) {
                Text("Aucun événement local", style = MaterialTheme.typography.bodySmall)
            } else {
                localLines.take(40).forEach { line ->
                    Text(line, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        stats?.let { s ->
            Text("7 derniers jours", style = MaterialTheme.typography.titleMedium)
            StatTileRow(
                listOf(
                    "Filtrés" to s.screened7d,
                    "Bloqués" to s.blocked7d,
                    "Autorisés" to s.allowed7d,
                ),
            )
            Text("30 derniers jours", style = MaterialTheme.typography.titleMedium)
            StatTileRow(
                listOf(
                    "Filtrés" to s.screened30d,
                    "Bloqués" to s.blocked30d,
                    "Autorisés" to s.allowed30d,
                ),
            )
            Text("Récents", style = MaterialTheme.typography.titleMedium)
            s.recent.take(20).forEach { e ->
                Text(
                    "${e.occurredAt} · ${actionLabel(e.action)} · ${e.prefix ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (s.recent.isEmpty()) {
                Text("Aucun événement récent", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
