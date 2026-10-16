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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.chatondearu.nmcc.NmccApp
import fr.chatondearu.nmcc.matcher.MatchType
import fr.chatondearu.nmcc.matcher.RuleEffect
import fr.chatondearu.nmcc.matcher.expandCountryPrefixPatterns
import fr.chatondearu.nmcc.network.SyncListEntry
import fr.chatondearu.nmcc.network.SyncRule
import kotlinx.coroutines.launch

private data class CountryOption(val callingCode: String, val label: String)

private val countryOptions = listOf(
    CountryOption("33", "France (+33)"),
    CountryOption("32", "Belgique (+32)"),
    CountryOption("41", "Suisse (+41)"),
    CountryOption("352", "Luxembourg (+352)"),
)

private enum class RulesTab { DEFAULT, PERSONAL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen() {
    val app = NmccApp.instance
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(RulesTab.PERSONAL) }
    var personalRules by remember { mutableStateOf<List<SyncRule>>(emptyList()) }
    var defaultEntries by remember { mutableStateOf<List<SyncListEntry>>(emptyList()) }
    var countryCode by remember { mutableStateOf("33") }
    var countryExpanded by remember { mutableStateOf(false) }
    var pattern by remember { mutableStateOf("") }
    var matchType by remember { mutableStateOf("prefix") }
    var effect by remember { mutableStateOf("block") }
    var ttl by remember { mutableStateOf("7d") }
    var message by remember { mutableStateOf<String?>(null) }

    val selectedCountry = countryOptions.firstOrNull { it.callingCode == countryCode } ?: countryOptions.first()
    val preview = remember(countryCode, pattern) {
        runCatching { expandCountryPrefixPatterns(countryCode, pattern) }.getOrDefault(emptyList())
    }

    fun reload() {
        scope.launch {
            personalRules = app.store.loadSyncRules()
            defaultEntries = app.store.loadAllListEntries()
        }
    }

    LaunchedEffect(Unit) { reload() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 24.dp),
    ) {
        item {
            Text("Rules", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Official FR ranges (bundled) and your personal rules. Toggle to enable/disable. " +
                    "Call Screening role must be active (Home).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = tab == RulesTab.DEFAULT,
                    onClick = { tab = RulesTab.DEFAULT },
                    label = { Text("Default FR (${defaultEntries.size})") },
                )
                FilterChip(
                    selected = tab == RulesTab.PERSONAL,
                    onClick = { tab = RulesTab.PERSONAL },
                    label = { Text("Personal (${personalRules.size})") },
                )
            }
        }

        if (tab == RulesTab.PERSONAL) {
            item {
                Text("Country code", style = MaterialTheme.typography.labelLarge)
                ExposedDropdownMenuBox(
                    expanded = countryExpanded,
                    onExpandedChange = { countryExpanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                ) {
                    OutlinedTextField(
                        value = selectedCountry.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Country") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(countryExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = countryExpanded,
                        onDismissRequest = { countryExpanded = false },
                    ) {
                        countryOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    countryCode = option.callingCode
                                    countryExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text("Local digits (e.g. 783 or 0783)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        if (preview.isNotEmpty()) {
                            Text("Will save: ${preview.joinToString(" · ")}")
                        }
                    },
                )
            }

            item {
                Text("Match type", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    listOf("prefix", "exact").forEach { option ->
                        FilterChip(
                            selected = matchType == option,
                            onClick = { matchType = option },
                            label = { Text(option) },
                        )
                    }
                }
            }

            item {
                Text("Effet", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    listOf("block" to "Bloquer", "allow" to "Autoriser").forEach { (value, label) ->
                        FilterChip(
                            selected = effect == value,
                            onClick = { effect = value },
                            label = { Text(label) },
                        )
                    }
                }
            }

            item {
                Text("TTL", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    listOf("1h", "24h", "7d", "30d", "permanent").forEach { option ->
                        FilterChip(
                            selected = ttl == option,
                            onClick = { ttl = option },
                            label = { Text(option) },
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            message = app.sync.createPersonalRule(
                                pattern = pattern,
                                matchType = matchType,
                                ttlPreset = ttl,
                                countryCallingCode = countryCode,
                                effect = effect,
                            ).fold(
                                onSuccess = {
                                    val saved = preview.joinToString(", ")
                                    pattern = ""
                                    reload()
                                    "Saved: $saved"
                                },
                                onFailure = { it.message ?: "Create failed" },
                            )
                        }
                    },
                    enabled = pattern.filter { it.isDigit() }.length >= 2,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Add personal rule")
                }
                if (message != null) {
                    Text(
                        message!!,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            items(personalRules, key = { it.id }) { rule ->
                RuleToggleRow(
                    title = rule.pattern,
                    subtitle = "${if (rule.effect == RuleEffect.ALLOW) "autoriser" else "bloquer"} · " +
                        "${if (rule.matchType == MatchType.EXACT) "exact" else "prefix"} · ${rule.ttlPreset}" +
                        (rule.expiresAt?.let { " · expires $it" } ?: " · permanent"),
                    checked = rule.active,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            app.store.setPersonalRuleActive(rule.id, enabled)
                            reload()
                        }
                    },
                    trailing = if (rule.effect == RuleEffect.BLOCK) {
                        {
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
                    } else {
                        null
                    },
                )
            }

            if (personalRules.isEmpty()) {
                item {
                    Text("No personal rules yet", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            item {
                Text(
                    "Bundled ARCEP NPV / demarchage prefixes. Disable entries you want to allow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(defaultEntries, key = { it.id }) { entry ->
                RuleToggleRow(
                    title = entry.pattern,
                    subtitle = "default · ${entry.id}",
                    checked = entry.active,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            app.store.setListEntryActive(entry.id, enabled)
                            reload()
                        }
                    },
                )
            }
            if (defaultEntries.isEmpty()) {
                item {
                    Text("Default list empty — use Refresh on Home", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun RuleToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        trailing?.invoke()
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
