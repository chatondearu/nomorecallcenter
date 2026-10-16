package fr.chatondearu.nmcc.ui.screens

import android.Manifest
import android.os.Build
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import fr.chatondearu.nmcc.NmccApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onOpenLogin: () -> Unit, onSignedOut: () -> Unit) {
    val app = NmccApp.instance
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var baseUrl by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var contactsOnly by remember { mutableStateOf(false) }
    var blockHidden by remember { mutableStateOf(false) }
    var notifyBlocked by remember { mutableStateOf(false) }

    val contactsPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        scope.launch {
            app.prefs.setContactsOnly(granted)
            contactsOnly = granted
        }
    }
    val notifyPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        scope.launch {
            app.prefs.setNotifyBlocked(granted)
            notifyBlocked = granted
        }
    }

    LaunchedEffect(Unit) {
        baseUrl = app.prefs.baseUrl.first()
        email = app.prefs.email.first()
        contactsOnly = app.prefs.contactsOnly.first()
        blockHidden = app.prefs.blockHiddenNumbers.first()
        notifyBlocked = app.prefs.notifyBlocked.first()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Paramètres", style = MaterialTheme.typography.headlineSmall)
        PrefSwitch(
            title = "Contacts uniquement",
            subtitle = "Les numéros hors contacts sont rejetés. La liste des contacts reste sur l’appareil.",
            checked = contactsOnly,
            onCheckedChange = { enabled ->
                if (!enabled) {
                    scope.launch {
                        app.prefs.setContactsOnly(false)
                        contactsOnly = false
                    }
                } else {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS,
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (granted) {
                    scope.launch {
                        app.prefs.setContactsOnly(true)
                        contactsOnly = true
                    }
                } else {
                    contactsPermission.launch(Manifest.permission.READ_CONTACTS)
                }
                }
            },
        )
        PrefSwitch(
            title = "Bloquer les numéros masqués",
            subtitle = "Rejeter les appels sans numéro.",
            checked = blockHidden,
            onCheckedChange = { enabled ->
                scope.launch {
                    app.prefs.setBlockHiddenNumbers(enabled)
                    blockHidden = enabled
                }
            },
        )
        PrefSwitch(
            title = "Notifier un appel bloqué",
            subtitle = "Désactivé par défaut. L’appel reste rejeté en silence ; une notification locale est ajoutée.",
            checked = notifyBlocked,
            onCheckedChange = { enabled ->
                if (!enabled) {
                    scope.launch {
                        app.prefs.setNotifyBlocked(false)
                        notifyBlocked = false
                    }
                } else if (Build.VERSION.SDK_INT >= 33) {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        scope.launch {
                            app.prefs.setNotifyBlocked(true)
                            notifyBlocked = true
                        }
                    } else {
                        notifyPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    scope.launch {
                        app.prefs.setNotifyBlocked(true)
                        notifyBlocked = true
                    }
                }
            },
        )
        Text(
            "Facultatif : indiquez l’URL d’une instance pour synchroniser règles et statistiques.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("URL de l’instance") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        if (email.isNotBlank()) {
            Text("Connecté en tant que $email", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text("Non connecté (hors ligne)", style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = {
                scope.launch {
                    app.prefs.setBaseUrl(baseUrl)
                    saved = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Enregistrer l’URL")
        }
        if (saved) {
            Text("Enregistré", style = MaterialTheme.typography.bodySmall)
        }
        OutlinedButton(
            onClick = onOpenLogin,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Se connecter à l’instance")
        }
        if (email.isNotBlank()) {
            Button(
                onClick = {
                    scope.launch {
                        app.api.clearCookies()
                        app.prefs.clearSessionLocal()
                        onSignedOut()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Se déconnecter")
            }
        }
    }
}

@Composable
private fun PrefSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
