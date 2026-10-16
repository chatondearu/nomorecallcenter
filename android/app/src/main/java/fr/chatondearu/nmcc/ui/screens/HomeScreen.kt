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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    var testBlocked by remember { mutableStateOf(false) }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        holdsRole = ScreeningRole.holdsCallScreeningRole(activity)
        statusMessage = if (holdsRole) {
            "Rôle de filtrage accordé — les appels entrants sont filtrés automatiquement"
        } else {
            "Rôle pas encore accordé. Une seule application de filtrage peut être active — choisissez NoMoreCallCenter dans Applications par défaut."
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
            smsGranted -> "Accès SMS accordé — filtre au mieux"
            smsDeniedHard -> "SMS bloqués par le système — Réglages de l’application → Autorisations → SMS → Autoriser"
            else -> "Autorisation SMS refusée — le filtrage des appels fonctionne sans SMS"
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
                    val actionLabel = when (action) {
                        "blocked" -> "bloqué"
                        "allowed" -> "autorisé"
                        else -> action
                    }
                    buildString {
                        append("Dernier filtrage : $actionLabel")
                        if (normalized.isNotBlank()) append(" · $normalized")
                        if (matched.isNotBlank()) append(" · règle $matched")
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
        Text("Accueil", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Préfixes NPV intégrés ($listCount) et règles personnelles. Le filtrage est automatique dès que le rôle est accordé.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (holdsRole) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                },
                contentColor = if (holdsRole) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                },
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    if (holdsRole) "Protection active" else "Protection inactive",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    if (holdsRole) {
                        "Cette application détient le rôle de filtrage des appels."
                    } else {
                        "Les appels ne sont pas filtrés."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!holdsRole && !roleAvailable) {
                    Text(
                        "Le rôle de filtrage des appels n’est pas disponible sur cet appareil.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Text("Filtrage des appels", style = MaterialTheme.typography.titleMedium)
        Text(
            "Ce n’est pas une autorisation d’application. C’est un rôle système (Applications par défaut → Filtrage d’appels / Identification et indésirables). L’autorisation SMS seule ne bloque pas les appels.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Button(
            onClick = {
                val intent = ScreeningRole.createRequestIntent(activity)
                if (intent != null) {
                    roleLauncher.launch(intent)
                } else {
                    statusMessage = "Impossible de demander le rôle de filtrage sur cet appareil"
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (holdsRole) "Confirmer le rôle de filtrage" else "Activer le filtrage des appels")
        }
        OutlinedButton(
            onClick = {
                ScreeningRole.openDefaultAppsSettings(activity)
                statusMessage = "Choisissez NoMoreCallCenter comme application de filtrage d’appels"
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Applications par défaut")
        }

        Text("Dernier appel filtré", style = MaterialTheme.typography.titleMedium)
        if (lastScreen == null) {
            Text(
                "Aucun pour l’instant. Si cela reste vide après un appel, Android n’a pas appelé cette application — le rôle n’est pas actif.",
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
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        line,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }

        Text("Simuler un appel", style = MaterialTheme.typography.titleMedium)
        Text(
            "Même correspondance que le service de filtrage (liste + règles personnelles). " +
                "Aucun appel réel — sert à vérifier les préfixes. Un appel réel exige le rôle et la ligne « Dernier appel filtré ».",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = testNumber,
            onValueChange = { testNumber = it },
            label = { Text("Numéro fictif (ex. +33783123456 ou 0783123456)") },
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
                    testBlocked = blocked
                    testResult = buildString {
                        append(if (blocked) "BLOQUÉ" else "AUTORISÉ")
                        decision.normalizedNumber?.let { append(" · normalisé $it") }
                        decision.matchedPattern?.let { append(" · correspond à $it") }
                            ?: append(" · aucune règle")
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
            Text("Vérifier ce numéro")
        }
        if (testResult != null) {
            Text(
                testResult!!,
                color = if (testBlocked) NmccColors.statusBlocked else NmccColors.statusAllowed,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Text("SMS (facultatif)", style = MaterialTheme.typography.titleMedium)
        if (smsGranted) {
            Text(
                "Accès SMS accordé — filtre au mieux.",
                color = NmccColors.statusAllowed,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Button(
                onClick = { smsPermissionLauncher.launch(smsPermissions) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Autoriser le filtrage SMS")
            }
            OutlinedButton(
                onClick = {
                    activity.openAppPermissionSettings()
                    statusMessage = "Activez les SMS pour NoMoreCallCenter, puis revenez ici"
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Autorisations de l’application (SMS)")
            }
            if (smsDeniedHard) {
                Text(
                    "SMS refusés définitivement — à activer dans les réglages.",
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
                        "Rôle=${if (holdsRole) "oui" else "non"} · SMS=${if (smsGranted) "oui" else "non"} · liste=$listCount"
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Actualiser")
        }
        if (statusMessage != null) {
            Text(statusMessage!!, style = MaterialTheme.typography.bodySmall)
        }
    }
}
