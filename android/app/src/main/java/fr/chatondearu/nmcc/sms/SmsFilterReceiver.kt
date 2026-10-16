package fr.chatondearu.nmcc.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import fr.chatondearu.nmcc.NmccApp
import fr.chatondearu.nmcc.matcher.ScreenAction
import fr.chatondearu.nmcc.matcher.decideScreen
import fr.chatondearu.nmcc.store.OfflineBootstrap
import kotlinx.coroutines.runBlocking

/**
 * Best-effort SMS filter via SMS_RECEIVED + abortBroadcast.
 * On modern Android this only works reliably when the app can receive the ordered broadcast
 * before the default SMS app — not a substitute for being the default SMS handler.
 */
class SmsFilterReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val app = NmccApp.instance
        val (list, rules) = runBlocking {
            OfflineBootstrap.ensureBundledList(app, app.prefs)
            app.store.loadListEntries() to app.store.loadPersonalRules()
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        for (msg in messages) {
            val decision = decideScreen(msg.originatingAddress, list, rules)
            if (decision.action == ScreenAction.BLOCK) {
                abortBroadcast()
                app.events.enqueueAfterScreen(decision)
                return
            }
        }
    }
}
