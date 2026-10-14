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

@Composable
fun StatsScreen() {
    val app = NmccApp.instance
    var scope by remember { mutableStateOf("personal") }
    var stats by remember { mutableStateOf<StatsBundle?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(scope) {
        error = null
        stats = null
        runCatching {
            withContext(Dispatchers.IO) {
                val base = app.prefs.getBaseUrl()
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
