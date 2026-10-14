package fr.chatondearu.nmcc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.chatondearu.nmcc.NmccApp
import fr.chatondearu.nmcc.matcher.MatchType
import fr.chatondearu.nmcc.network.SyncRule
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen() {
    val app = NmccApp.instance
    val scope = rememberCoroutineScope()
    var rules by remember { mutableStateOf<List<SyncRule>>(emptyList()) }
    var pattern by remember { mutableStateOf("") }
    var matchType by remember { mutableStateOf("prefix") }
    var ttl by remember { mutableStateOf("7d") }
    var message by remember { mutableStateOf<String?>(null) }
    var matchExpanded by remember { mutableStateOf(false) }
    var ttlExpanded by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch { rules = app.store.loadSyncRules() }
    }

    LaunchedEffect(Unit) { reload() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Personal rules", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Blocks apply immediately after sync. Propose to global stays moderated (ADR-007).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = pattern,
            onValueChange = { pattern = it },
            label = { Text("Pattern (digits)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        ExposedDropdownMenuBox(expanded = matchExpanded, onExpandedChange = { matchExpanded = it }) {
            OutlinedTextField(
                value = matchType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Match type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(matchExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            androidx.compose.material3.ExposedDropdownMenu(
                expanded = matchExpanded,
                onDismissRequest = { matchExpanded = false },
            ) {
                listOf("prefix", "exact").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            matchType = option
                            matchExpanded = false
                        },
                    )
                }
            }
        }

        ExposedDropdownMenuBox(expanded = ttlExpanded, onExpandedChange = { ttlExpanded = it }) {
            OutlinedTextField(
                value = ttl,
                onValueChange = {},
                readOnly = true,
                label = { Text("TTL") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(ttlExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            androidx.compose.material3.ExposedDropdownMenu(
                expanded = ttlExpanded,
                onDismissRequest = { ttlExpanded = false },
            ) {
                listOf("1h", "24h", "7d", "30d", "permanent").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            ttl = option
                            ttlExpanded = false
                        },
                    )
                }
            }
        }

        Button(
            onClick = {
                scope.launch {
                    message = app.sync.createPersonalRule(pattern, matchType, ttl).fold(
                        onSuccess = {
                            pattern = ""
                            reload()
                            "Rule created and synced"
                        },
                        onFailure = { it.message ?: "Create failed" },
                    )
                }
            },
            enabled = pattern.length >= 2,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add personal rule")
        }

        if (message != null) {
            Text(message!!, style = MaterialTheme.typography.bodySmall)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rules, key = { it.id }) { rule ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(rule.pattern, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${if (rule.matchType == MatchType.EXACT) "exact" else "prefix"} · ${rule.ttlPreset}" +
                                (rule.expiresAt?.let { " · expires $it" } ?: " · permanent"),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(
                        onClick = {
                            scope.launch {
                                message = app.sync.proposeToGlobal(rule.id).fold(
                                    onSuccess = { "Proposed to global (pending moderation)" },
                                    onFailure = { it.message ?: "Propose failed" },
                                )
                            }
                        },
                    ) {
                        Text("Propose")
                    }
                }
            }
        }
    }
}
