package fr.chatondearu.nmcc.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import fr.chatondearu.nmcc.NmccApp
import fr.chatondearu.nmcc.matcher.ScreenAction
import fr.chatondearu.nmcc.screening.ScreeningRole
import fr.chatondearu.nmcc.screening.decideIncoming
import fr.chatondearu.nmcc.store.OfflineBootstrap
import fr.chatondearu.nmcc.ui.theme.NmccColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

private val smsPermissions = arrayOf(
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.READ_SMS,
)

private fun Activity.hasSmsAccess(): Boolean =
    smsPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

private fun Activity.openAppPermissionSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null),
        ),
    )
}

@Composable
fun HomeScreen(activity: Activity) {
    val app = NmccApp.instance
    val scope = rememberCoroutineScope()
    var holdsRole by remember { mutableStateOf(ScreeningRole.holdsCallScreeningRole(activity)) }
    var roleAvailable by remember { mutableStateOf(ScreeningRole.isRoleAvailable(activity)) }
    var listCount by remember { mutableStateOf(0) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var smsGranted by remember { mutableStateOf(activity.hasSmsAccess()) }
    var smsDeniedHard by remember { mutableStateOf(false) }
    var lastScreen by remember { mutableStateOf<String?>(null) }
    var journal by remember { mutableStateOf<List<String>>(emptyList()) }
    var testNumber by remember { mutableStateOf("0783123456") }
    var testResult by remember { mutableStateOf<String?>(null) }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        holdsRole = ScreeningRole.holdsCallScreeningRole(activity)
        statusMessage = if (holdsRole) {
            "Call screening role granted — incoming calls are filtered automatically"
        } else {
            "Role not granted yet. Only one call-screening app can be active — set NoMoreCallCenter in Default apps."
        }
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        smsGranted = result.values.all { it }
        smsDeniedHard = !smsGranted &&
            smsPermissions.any {
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
            }
        statusMessage = when {
            smsGranted -> "SMS access granted — best-effort filter active"
            smsDeniedHard -> "SMS blocked by system — open App settings → Permissions → SMS → Allow"
            else -> "SMS permission denied — call screening still works without SMS"
        }
    }

    fun refreshRole() {
        holdsRole = ScreeningRole.holdsCallScreeningRole(activity)
        roleAvailable = ScreeningRole.isRoleAvailable(activity)
    }

    fun refreshSms() {
        smsGranted = activity.hasSmsAccess()
        if (smsGranted) smsDeniedHard = false
    }

    fun refreshJournal() {
        scope.launch {
            val raw = app.prefs.blockedJournalJson.first()
            journal = runCatching {
                val arr = JSONArray(raw)
                buildList {
                    for (i in arr.length() - 1 downTo 0) {
                        val o = arr.getJSONObject(i)
                        val number = o.optString("number")
                        val pattern = o.optString("pattern")
                        val at = o.optString("at")
                        add(
                            buildString {
                                append(number.ifBlank { "Numéro masqué" })
                                if (pattern.isNotBlank()) append(" · $pattern")
                                if (at.isNotBlank()) append(" · $at")
                            },
                        )
                    }
                }
            }.getOrDefault(emptyList())
        }
    }

    fun refreshLastScreen() {
        scope.launch {
            val raw = app.prefs.lastScreenJson.first()
            lastScreen = if (raw.isBlank()) {
                null
            } else {
                runCatching {
                    val o = JSONObject(raw)
                    val action = o.optString("action")
                    val normalized = o.optString("normalized").ifBlank { o.optString("raw") }
                    val matched = o.optString("matched")
                    val at = o.optString("at")
                    buildString {
                        append("Last screened: $action")
                        if (normalized.isNotBlank()) append(" · $normalized")
                        if (matched.isNotBlank()) append(" · matched $matched")
                        if (at.isNotBlank()) append("\n$at")
                    }
                }.getOrNull()
            }
        }
    }

    fun refreshListCount() {
        scope.launch {
            OfflineBootstrap.ensureBundledList(activity, app.prefs)
            listCount = app.store.loadListEntries().size
        }
    }

    LaunchedEffect(Unit) {
        refreshListCount()
        refreshLastScreen()
        refreshJournal()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshRole()
                refreshSms()
                refreshLastScreen()
                refreshJournal()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Call & SMS filter", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Bundled NPV prefixes ($listCount) + personal rules. Filtering is automatic once the call-screening role is held.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text("1. Call screening (required)", style = MaterialTheme.typography.titleMedium)
        Text(
            "This is NOT listed under App permissions. It is a system role (Default apps → Call screening / Caller ID & spam). SMS permission alone does not block calls.",
            style = MaterialTheme.typography.bodyMedium,
        )

        if (holdsRole) {
            Text(
                "Protected — this app holds the call screening role.",
                color = NmccColors.statusAllowed,
                style = MaterialTheme.typography.titleMedium,
            )
        } else {
            Text(
                "NOT protected — calls are NOT filtered.",
                color = NmccColors.statusBlocked,
                style = MaterialTheme.typography.titleMedium,
            )
            if (!roleAvailable) {
                Text(
                    "ROLE_CALL_SCREENING is unavailable on this device/OEM.",
                    color = NmccColors.statusBlocked,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Button(
            onClick = {
                val intent = ScreeningRole.createRequestIntent(activity)
                if (intent != null) {
                    roleLauncher.launch(intent)
                } else {
                    statusMessage = "Cannot request call screening role on this device"
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (holdsRole) "Re-confirm call screening role" else "Enable call screening role")
        }
        OutlinedButton(
            onClick = {
                ScreeningRole.openDefaultAppsSettings(activity)
                statusMessage = "Pick NoMoreCallCenter as Call screening / Caller ID & spam app"
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Open Default apps settings")
        }

        Text("Last screened call", style = MaterialTheme.typography.titleMedium)
        if (lastScreen == null) {
            Text(
                "None yet. After a call, if this stays empty, Android never invoked this app — the role is not active (or the OEM skipped screening).",
                style = MaterialTheme.typography.bodyMedium,
                color = NmccColors.statusBlocked,
            )
        } else {
            Text(lastScreen!!, style = MaterialTheme.typography.bodyMedium)
        }

        Text("Journal des appels bloqués", style = MaterialTheme.typography.titleMedium)
        if (journal.isEmpty()) {
            Text(
                "Aucun appel bloqué enregistré sur cet appareil.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            journal.take(20).forEach { line ->
                Text(line, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Text("2. Simulate incoming call", style = MaterialTheme.typography.titleMedium)
        Text(
            "Runs the same matcher as CallScreeningService (list + personal rules). " +
                "Does not place a real call — use this to verify prefixes. Real-call proof still needs the role + Last screened call.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = testNumber,
            onValueChange = { testNumber = it },
            label = { Text("Fake caller (e.g. +33783123456 or 0783123456)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(
            onClick = {
                scope.launch {
                    OfflineBootstrap.ensureBundledList(activity, app.prefs)
                    val decision = decideIncoming(activity, app.prefs, app.store, testNumber)
                    val blocked = decision.action == ScreenAction.BLOCK
                    val at = Instant.now().toString()
                    if (blocked) {
                        app.prefs.appendBlockedJournal(
                            decision.normalizedNumber ?: "Numéro masqué",
                            decision.matchedPattern,
                            at,
                        )
                    }
                    testResult = buildString {
                        append(if (blocked) "BLOCK" else "ALLOW")
                        decision.normalizedNumber?.let { append(" · normalized $it") }
                        decision.matchedPattern?.let { append(" · matched $it") }
                            ?: append(" · no rule matched")
                    }
                    app.prefs.setLastScreenJson(
                        JSONObject()
                            .put("action", if (blocked) "blocked" else "allowed")
                            .put("raw", testNumber)
                            .put("normalized", decision.normalizedNumber ?: "")
                            .put("matched", decision.matchedPattern ?: "")
                            .put("at", at)
                            .put("source", "simulate")
                            .toString(),
                    )
                    refreshLastScreen()
                    refreshJournal()
                }
            },
            enabled = testNumber.filter { it.isDigit() }.length >= 2,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Run spam check")
        }
        if (testResult != null) {
            Text(
                testResult!!,
                color = if (testResult!!.startsWith("BLOCK")) {
                    NmccColors.statusBlocked
                } else {
                    NmccColors.statusAllowed
                },
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Text("3. SMS (optional)", style = MaterialTheme.typography.titleMedium)
        if (smsGranted) {
            Text(
                "SMS access granted — best-effort filter.",
                color = NmccColors.statusAllowed,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Button(
                onClick = { smsPermissionLauncher.launch(smsPermissions) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Allow SMS filtering")
            }
            OutlinedButton(
                onClick = {
                    activity.openAppPermissionSettings()
                    statusMessage = "Enable SMS for NoMoreCallCenter, then return here"
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Open app permissions (SMS)")
            }
            if (smsDeniedHard) {
                Text(
                    "SMS permanently denied — toggle manually in settings.",
                    color = NmccColors.statusBlocked,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        OutlinedButton(
            onClick = {
                scope.launch {
                    refreshRole()
                    refreshSms()
                    refreshLastScreen()
                    OfflineBootstrap.ensureBundledList(activity, app.prefs)
                    listCount = app.store.loadListEntries().size
                    statusMessage =
                        "Role=${if (holdsRole) "YES" else "NO"} · SMS=${if (smsGranted) "YES" else "NO"} · list=$listCount"
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Refresh status")
        }
        if (statusMessage != null) {
            Text(statusMessage!!, style = MaterialTheme.typography.bodySmall)
        }
    }
}
