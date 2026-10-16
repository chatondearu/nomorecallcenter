package fr.chatondearu.nmcc.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import fr.chatondearu.nmcc.NmccApp
import fr.chatondearu.nmcc.matcher.ScreenAction
import fr.chatondearu.nmcc.matcher.decideScreen
import fr.chatondearu.nmcc.store.OfflineBootstrap
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.time.Instant

/**
 * Native CallScreeningService (ADR-003). Silent reject on match (ADR-001).
 * Loads cached list + personal rules; responds quickly within the platform budget.
 */
class NmccCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val app = NmccApp.instance
        val raw = callDetails.handle?.schemeSpecificPart
        val (list, rules) = runBlocking {
            OfflineBootstrap.ensureBundledList(app, app.prefs)
            app.store.loadListEntries() to app.store.loadPersonalRules()
        }
        val decision = decideScreen(raw, list, rules)
        val block = decision.action == ScreenAction.BLOCK
        val response = CallResponse.Builder()
            .setDisallowCall(block)
            .setRejectCall(block)
            .setSkipCallLog(block)
            .setSkipNotification(block)
            .build()
        respondToCall(callDetails, response)

        runBlocking {
            app.prefs.setLastScreenJson(
                JSONObject()
                    .put("action", if (block) "blocked" else "allowed")
                    .put("raw", raw ?: "")
                    .put("normalized", decision.normalizedNumber ?: "")
                    .put("matched", decision.matchedPattern ?: "")
                    .put("at", Instant.now().toString())
                    .toString(),
            )
        }
        app.events.enqueueAfterScreen(decision)
    }
}
