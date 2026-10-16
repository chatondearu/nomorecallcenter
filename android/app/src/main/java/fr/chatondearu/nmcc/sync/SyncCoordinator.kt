package fr.chatondearu.nmcc.sync

import fr.chatondearu.nmcc.matcher.expandCountryPrefixPatterns
import fr.chatondearu.nmcc.store.LocalRuleStore
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

    /**
     * @param countryCallingCode dial code without '+' (e.g. `33`); expands FR to national + intl forms.
     */
    suspend fun createPersonalRule(
        pattern: String,
        matchType: String,
        ttlPreset: String,
        countryCallingCode: String = "33",
        effect: String = "block",
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val patterns = expandCountryPrefixPatterns(countryCallingCode, pattern)
                val base = prefs.getBaseUrl()
                if (base.isBlank() || !api.hasSessionCookie(base)) {
                    store.addLocalRules(patterns, matchType, ttlPreset, effect)
                    return@runCatching
                }
                for (p in patterns) {
                    api.createRule(base, p, matchType, ttlPreset, effect)
                }
                val rules = api.syncRules(base)
                store.saveRules(rules)
            }
        }

    suspend fun proposeToGlobal(personalRuleId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val base = prefs.getBaseUrl()
            require(base.isNotBlank() && api.hasSessionCookie(base)) {
                "Sign in to an instance to propose rules"
            }
            api.propose(base, personalRuleId)
        }
    }
}
