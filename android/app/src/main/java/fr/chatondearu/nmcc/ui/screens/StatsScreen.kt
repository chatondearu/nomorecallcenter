package fr.chatondearu.nmcc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
                            "${o.optString("occurredAt")} · ${o.optString("action")} · " +
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
                require(base.isNotBlank()) { "Set an instance URL in Settings" }
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
        Text("Stats", style = MaterialTheme.typography.headlineSmall)
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = scope == "local",
                onClick = { scope = "local" },
                label = { Text("Local queue") },
            )
            FilterChip(
                selected = scope == "personal",
                onClick = { scope = "personal" },
                label = { Text("Personal") },
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
            Text("Pending / recent local screen events", style = MaterialTheme.typography.titleMedium)
            if (localLines.isEmpty()) {
                Text("No local events yet", style = MaterialTheme.typography.bodySmall)
            } else {
                localLines.take(40).forEach { line ->
                    Text(line, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        stats?.let { s ->
            Text("Last 7 days", style = MaterialTheme.typography.titleMedium)
            Text("Screened ${s.screened7d} · Blocked ${s.blocked7d} · Allowed ${s.allowed7d}")
            Text("Last 30 days", style = MaterialTheme.typography.titleMedium)
            Text("Screened ${s.screened30d} · Blocked ${s.blocked30d} · Allowed ${s.allowed30d}")
            Text("Recent", style = MaterialTheme.typography.titleMedium)
            s.recent.take(20).forEach { e ->
                Text(
                    "${e.occurredAt} · ${e.action} · ${e.prefix ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (s.recent.isEmpty()) {
                Text("No recent events", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
