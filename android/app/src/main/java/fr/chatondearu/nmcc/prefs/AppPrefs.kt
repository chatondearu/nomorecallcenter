package fr.chatondearu.nmcc.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "nmcc_prefs")

class AppPrefs(private val context: Context) {
    private val keyBaseUrl = stringPreferencesKey("base_url")
    private val keyEmail = stringPreferencesKey("email")
    private val keyListRevision = longPreferencesKey("list_revision")
    private val keyListJson = stringPreferencesKey("list_entries_json")
    private val keyRulesJson = stringPreferencesKey("personal_rules_json")
    private val keyEventQueue = stringPreferencesKey("event_queue_json")

    val baseUrl: Flow<String> = context.dataStore.data.map { it[keyBaseUrl] ?: "" }
    val email: Flow<String> = context.dataStore.data.map { it[keyEmail] ?: "" }

    suspend fun getBaseUrl(): String = baseUrl.first()
    suspend fun getEmail(): String = email.first()
    suspend fun getListRevision(): Long =
        context.dataStore.data.first()[keyListRevision] ?: 0L

    suspend fun getListJson(): String =
        context.dataStore.data.first()[keyListJson] ?: "[]"

    suspend fun getRulesJson(): String =
        context.dataStore.data.first()[keyRulesJson] ?: "[]"

    suspend fun getEventQueueJson(): String =
        context.dataStore.data.first()[keyEventQueue] ?: "[]"

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { it[keyBaseUrl] = url.trim().trimEnd('/') }
    }

    suspend fun setEmail(value: String) {
        context.dataStore.edit { it[keyEmail] = value.trim() }
    }

    suspend fun setListSnapshot(revision: Long, entriesJson: String) {
        context.dataStore.edit {
            it[keyListRevision] = revision
            it[keyListJson] = entriesJson
        }
    }

    suspend fun setRulesJson(json: String) {
        context.dataStore.edit { it[keyRulesJson] = json }
    }

    suspend fun setEventQueueJson(json: String) {
        context.dataStore.edit { it[keyEventQueue] = json }
    }

    suspend fun clearSessionLocal() {
        context.dataStore.edit {
            it.remove(keyEmail)
            it.remove(keyListRevision)
            it.remove(keyListJson)
            it.remove(keyRulesJson)
            it.remove(keyEventQueue)
        }
    }
}
