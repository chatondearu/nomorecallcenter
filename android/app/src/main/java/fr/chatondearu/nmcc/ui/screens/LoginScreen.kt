package fr.chatondearu.nmcc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import fr.chatondearu.nmcc.NmccApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    val app = NmccApp.instance
    val scope = rememberCoroutineScope()
    var baseUrl by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        baseUrl = app.prefs.baseUrl.first()
        email = app.prefs.email.first()
        if (baseUrl.isNotBlank() && app.api.hasSessionCookie(baseUrl)) {
            runCatching {
                withContext(Dispatchers.IO) { app.api.me(baseUrl) }
                onLoggedIn()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("No More Call Center", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Self-hosted instance login",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("Instance URL") },
            placeholder = { Text("http://192.168.1.10:3000") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                scope.launch {
                    loading = true
                    error = null
                    try {
                        val url = baseUrl.trim().trimEnd('/')
                        withContext(Dispatchers.IO) {
                            app.prefs.setBaseUrl(url)
                            app.prefs.setEmail(email)
                            app.api.signIn(url, email.trim(), password)
                            app.api.me(url)
                            app.sync.syncAll().getOrThrow()
                            app.events.flush()
                        }
                        onLoggedIn()
                    } catch (e: Exception) {
                        error = e.message ?: "Login failed"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading && baseUrl.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (loading) CircularProgressIndicator() else Text("Sign in")
        }
    }
}
