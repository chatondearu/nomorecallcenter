package fr.chatondearu.nmcc.sync

import fr.chatondearu.nmcc.data.LocalRuleStore
import fr.chatondearu.nmcc.network.ApiClient
import fr.chatondearu.nmcc.prefs.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncCoordinator(
    private val api: ApiClient,
    private val prefs: AppPrefs,
    private val store: LocalRuleStore,
) {
    suspend fun syncAll(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val base = prefs.getBaseUrl()
            require(base.isNotBlank()) { "Instance URL not set" }

            val since = prefs.getListRevision()
            val (revision, entries) = api.syncLists(base, since)
            store.applyListSync(revision, since, entries)

            val rules = api.syncRules(base)
            store.saveRules(rules)
        }
    }

    suspend fun createPersonalRule(pattern: String, matchType: String, ttlPreset: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val base = prefs.getBaseUrl()
                api.createRule(base, pattern, matchType, ttlPreset)
                val rules = api.syncRules(base)
                store.saveRules(rules)
            }
        }

    suspend fun proposeToGlobal(personalRuleId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            api.propose(prefs.getBaseUrl(), personalRuleId)
        }
    }
}
