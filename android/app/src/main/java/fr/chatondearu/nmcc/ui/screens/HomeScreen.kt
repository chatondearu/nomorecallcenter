package fr.chatondearu.nmcc.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.chatondearu.nmcc.NmccApp
import fr.chatondearu.nmcc.screening.ScreeningRole
import fr.chatondearu.nmcc.ui.theme.NmccColors
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(activity: Activity) {
    val app = NmccApp.instance
    val scope = rememberCoroutineScope()
    var holdsRole by remember { mutableStateOf(ScreeningRole.holdsCallScreeningRole(activity)) }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    fun refreshRole() {
        holdsRole = ScreeningRole.holdsCallScreeningRole(activity)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Call screening", style = MaterialTheme.typography.headlineSmall)

        if (holdsRole) {
            Text(
                "Protected — call screening role is active.",
                color = NmccColors.statusAllowed,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "Matching numbers are silently rejected using the shared list and your active personal rules.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Text(
                "Not protected yet",
                color = NmccColors.statusBlocked,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "Grant the Call Screening role so this app can filter incoming calls. " +
                    "We will not claim you are protected until the role is held (ADR-008).",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = {
                    ScreeningRole.requestCallScreeningRole(activity)
                    refreshRole()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Enable call screening")
            }
            OutlinedButton(
                onClick = { refreshRole() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Refresh role status")
            }
        }

        Button(
            onClick = {
                scope.launch {
                    refreshRole()
                    syncMessage = app.sync.syncAll().fold(
                        onSuccess = { "Synced lists + personal rules" },
                        onFailure = { it.message ?: "Sync failed" },
                    )
                    app.events.flush()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Sync now")
        }
        if (syncMessage != null) {
            Text(syncMessage!!, style = MaterialTheme.typography.bodySmall)
        }
    }
}
