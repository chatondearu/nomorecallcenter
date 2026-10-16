package fr.chatondearu.nmcc.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import fr.chatondearu.nmcc.NmccApp
import fr.chatondearu.nmcc.matcher.ScreenAction
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
        val decision = runBlocking {
            OfflineBootstrap.ensureBundledList(app, app.prefs)
            decideIncoming(this@NmccCallScreeningService, app.prefs, app.store, raw)
        }
        val block = decision.action == ScreenAction.BLOCK
        val response = CallResponse.Builder()
            .setDisallowCall(block)
            .setRejectCall(block)
            .setSkipCallLog(block)
            .setSkipNotification(block)
            .build()
        respondToCall(callDetails, response)

        val at = Instant.now().toString()
        runBlocking {
            app.prefs.setLastScreenJson(
                JSONObject()
                    .put("action", if (block) "blocked" else "allowed")
                    .put("raw", raw ?: "")
                    .put("normalized", decision.normalizedNumber ?: "")
                    .put("matched", decision.matchedPattern ?: "")
                    .put("at", at)
                    .toString(),
            )
            if (block) {
                val shown = decision.normalizedNumber ?: "Numéro masqué"
                app.prefs.appendBlockedJournal(shown, decision.matchedPattern, at)
                if (app.prefs.getNotifyBlocked()) {
                    postBlockedCallNotification(this@NmccCallScreeningService, shown, decision.matchedPattern)
                }
            }
        }
        app.events.enqueueAfterScreen(decision)
    }
}
