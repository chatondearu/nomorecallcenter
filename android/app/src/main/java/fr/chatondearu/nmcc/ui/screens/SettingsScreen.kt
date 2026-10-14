package fr.chatondearu.nmcc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onSignedOut: () -> Unit) {
    val app = NmccApp.instance
    val scope = rememberCoroutineScope()
    var baseUrl by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        baseUrl = app.prefs.baseUrl.first()
        email = app.prefs.email.first()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("Instance URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Text("Signed in as $email", style = MaterialTheme.typography.bodyMedium)
        Button(
            onClick = {
                scope.launch {
                    app.prefs.setBaseUrl(baseUrl)
                    saved = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save URL")
        }
        if (saved) {
            Text("Saved", style = MaterialTheme.typography.bodySmall)
        }
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
            Text("Sign out")
        }
    }
}
