package fr.chatondearu.nmcc.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import fr.chatondearu.nmcc.NmccApp
import fr.chatondearu.nmcc.matcher.decideScreen
import kotlinx.coroutines.runBlocking

/**
 * Native CallScreeningService (ADR-003). Silent reject on match (ADR-001).
 * Loads cached list + personal rules; responds quickly within the platform budget.
 */
class NmccCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val app = NmccApp.instance
        val raw = callDetails.handle?.schemeSpecificPart
        val (list, rules) = runBlocking {
            app.store.loadListEntries() to app.store.loadPersonalRules()
        }
        val decision = decideScreen(raw, list, rules)
        val block = decision.action == fr.chatondearu.nmcc.matcher.ScreenAction.BLOCK
        val response = CallResponse.Builder()
            .setDisallowCall(block)
            .setRejectCall(block)
            // Silent reject: no ringtone UI / notification / call-log noise when blocked
            .setSkipCallLog(block)
            .setSkipNotification(block)
            .build()
        respondToCall(callDetails, response)
        app.events.enqueueAfterScreen(decision)
    }
}
