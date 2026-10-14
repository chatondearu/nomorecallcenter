package fr.chatondearu.nmcc

import android.app.Application
import fr.chatondearu.nmcc.store.LocalRuleStore
import fr.chatondearu.nmcc.network.ApiClient
import fr.chatondearu.nmcc.prefs.AppPrefs
import fr.chatondearu.nmcc.sync.EventReporter
import fr.chatondearu.nmcc.sync.SyncCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NmccApp : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var prefs: AppPrefs
        private set
    lateinit var api: ApiClient
        private set
    lateinit var store: LocalRuleStore
        private set
    lateinit var sync: SyncCoordinator
        private set
    lateinit var events: EventReporter
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = AppPrefs(this)
        api = ApiClient()
        store = LocalRuleStore(prefs)
        sync = SyncCoordinator(api, prefs, store)
        events = EventReporter(api, prefs, appScope)
    }

    companion object {
        lateinit var instance: NmccApp
            private set
    }
}
