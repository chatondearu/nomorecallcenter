package fr.chatondearu.nmcc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import fr.chatondearu.nmcc.NmccApp
import fr.chatondearu.nmcc.network.PrefixCount
import fr.chatondearu.nmcc.network.StatsBundle
import fr.chatondearu.nmcc.ui.components.ActivityRow
import fr.chatondearu.nmcc.ui.components.formatActivityTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private data class JournalHit(val number: String, val pattern: String, val at: Instant)

private val dayLabels = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")

private fun parseJournal(raw: String): List<JournalHit> {
    val arr = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
    return buildList {
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val at = runCatching { Instant.parse(o.optString("at")) }.getOrNull() ?: continue
            add(
                JournalHit(
                    number = o.optString("number").ifBlank { "Numéro masqué" },
                    pattern = o.optString("pattern"),
                    at = at,
                ),
            )
        }
    }
}

private fun inLastDays(at: Instant, days: Long, now: Instant): Boolean =
    !at.isBefore(now.minus(days, ChronoUnit.DAYS))

/** Seven bars ending today, Monday-first labels, counts of blocks per local day. */
private fun sevenDayBlockCounts(hits: List<JournalHit>, today: LocalDate, zone: ZoneId): List<Pair<String, Int>> =
    (6 downTo 0).map { ago ->
        val day = today.minusDays(ago.toLong())
        val label = dayLabels[day.dayOfWeek.value - 1]
        val count = hits.count { it.at.atZone(zone).toLocalDate() == day }
        label to count
    }

private fun topPatterns(hits: List<JournalHit>, now: Instant): List<PrefixCount> =
    hits.filter { inLastDays(it.at, 30, now) && it.pattern.isNotBlank() }
        .groupingBy { it.pattern }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(5)
        .map { PrefixCount(it.key, it.value) }

@Composable
private fun BigNumber(value: Int, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value.toString(), style = MaterialTheme.typography.displaySmall)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun StatsScreen() {
    val app = NmccApp.instance
    var scope by remember { mutableStateOf("local") }
    var stats by remember { mutableStateOf<StatsBundle?>(null) }
    var journal by remember { mutableStateOf<List<JournalHit>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val now = Instant.now()

    LaunchedEffect(scope) {
        error = null
        stats = null
        journal = withContext(Dispatchers.IO) { parseJournal(app.prefs.getBlockedJournalJson()) }
        if (scope == "local") return@LaunchedEffect
        runCatching {
            withContext(Dispatchers.IO) {
                val base = app.prefs.getBaseUrl()
                require(base.isNotBlank()) { "Indiquez l’URL de l’instance dans Paramètres" }
                if (scope == "personal") app.api.statsMe(base) else app.api.statsInstance(base)
            }
        }.onSuccess { stats = it }
            .onFailure { error = it.message }
    }

    val blocked7 = journal.count { inLastDays(it.at, 7, now) }
    val blocked30 = journal.count { inLastDays(it.at, 30, now) }
    val remote = stats
    val showRemote = scope != "local" && remote != null
    val bars = sevenDayBlockCounts(journal, today, zone)
    val maxBar = bars.maxOf { it.second }.coerceAtLeast(1)
    val prefixes = if (showRemote && remote.topPrefixes.isNotEmpty()) remote.topPrefixes else topPatterns(journal, now)
    val prefixMax = prefixes.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Statistiques", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = scope == "local", onClick = { scope = "local" }, label = { Text("Cet appareil") })
            FilterChip(selected = scope == "personal", onClick = { scope = "personal" }, label = { Text("Personnel") })
            FilterChip(selected = scope == "instance", onClick = { scope = "instance" }, label = { Text("Instance") })
        }
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            BigNumber(
                if (showRemote) remote.blocked7d else blocked7,
                "Rejetés · 7 j",
                Modifier.weight(1f),
            )
            BigNumber(
                if (showRemote) remote.blocked30d else blocked30,
                "Rejetés · 30 j",
                Modifier.weight(1f),
            )
        }
        if (showRemote) {
            Text(
                "Filtrés ${remote.screened7d} · autorisés ${remote.allowed7d} (7 j)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text("Activité sur 7 jours", style = MaterialTheme.typography.titleMedium)
        Text(
            "Blocages enregistrés sur cet appareil",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEach { (label, count) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(count.toString(), style = MaterialTheme.typography.labelSmall)
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .width(18.dp)
                            .height((96f * count / maxBar).dp.coerceAtLeast(if (count == 0) 2.dp else 8.dp))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    )
                    Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        Text("Top des préfixes", style = MaterialTheme.typography.titleMedium)
        if (prefixes.isEmpty()) {
            Text("Aucun préfixe", style = MaterialTheme.typography.bodySmall)
        } else {
            prefixes.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.prefix, style = MaterialTheme.typography.titleSmall)
                        Text("${item.count}", style = MaterialTheme.typography.labelLarge)
                    }
                    LinearProgressIndicator(
                        progress = { item.count.toFloat() / prefixMax },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Text("Activité récente", style = MaterialTheme.typography.titleMedium)
        if (journal.isEmpty()) {
            Text("Aucun appel bloqué enregistré sur cet appareil.", style = MaterialTheme.typography.bodySmall)
        } else {
            journal.sortedByDescending { it.at }.take(20).forEach { hit ->
                ActivityRow(
                    number = hit.number,
                    reason = hit.pattern.ifBlank { "Rejet silencieux" },
                    time = formatActivityTime(hit.at.toString()),
                    status = "Rejeté",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
