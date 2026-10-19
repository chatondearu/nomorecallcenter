package fr.chatondearu.nmcc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.chatondearu.nmcc.ui.NmccNav
import fr.chatondearu.nmcc.ui.theme.NmccTheme

class MainActivity : ComponentActivity() {
    private var designRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        designRoute = intent.designRouteExtra()
        enableEdgeToEdge()
        setContent {
            NmccTheme {
                NmccNav(activity = this, designRoute = designRoute)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.designRouteExtra()?.let { designRoute = it }
    }

    private fun Intent.designRouteExtra(): String? =
        getStringExtra(EXTRA_DESIGN_ROUTE)?.takeIf { it in DESIGN_ROUTES }

    companion object {
        const val EXTRA_DESIGN_ROUTE = "NMCC_DESIGN_ROUTE"

        /** Routes reachable via `adb shell am start … --es NMCC_DESIGN_ROUTE <route>` (design captures). */
        val DESIGN_ROUTES = setOf("home", "rules", "stats", "settings", "login")
    }
}
