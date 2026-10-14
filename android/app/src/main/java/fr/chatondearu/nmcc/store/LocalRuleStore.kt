package fr.chatondearu.nmcc.store

import fr.chatondearu.nmcc.matcher.ListEntry
import fr.chatondearu.nmcc.matcher.MatchType
import fr.chatondearu.nmcc.matcher.PersonalRule
import fr.chatondearu.nmcc.network.SyncListEntry
import fr.chatondearu.nmcc.network.SyncRule
import fr.chatondearu.nmcc.prefs.AppPrefs
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/**
 * Persists synced list entries + personal rules for CallScreeningService (offline).
 */
class LocalRuleStore(private val prefs: AppPrefs) {

    suspend fun loadListEntries(): List<ListEntry> {
        val arr = JSONArray(prefs.getListJson())
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                if (!o.optBoolean("active", true)) continue
                add(ListEntry(pattern = o.getString("pattern"), active = true))
            }
        }
    }

    suspend fun loadPersonalRules(nowEpochMs: Long = System.currentTimeMillis()): List<PersonalRule> {
        val arr = JSONArray(prefs.getRulesJson())
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val expires = if (o.isNull("expiresAt")) null else parseIso(o.getString("expiresAt"))
                add(
                    PersonalRule(
                        pattern = o.getString("pattern"),
                        matchType = if (o.getString("matchType").equals("exact", true)) {
                            MatchType.EXACT
                        } else {
                            MatchType.PREFIX
                        },
                        expiresAtEpochMs = expires,
                    ),
                )
            }
        }.filter { rule ->
            rule.expiresAtEpochMs == null || rule.expiresAtEpochMs > nowEpochMs
        }
    }

    suspend fun applyListSync(revision: Long, since: Long, incoming: List<SyncListEntry>) {
        if (since == 0L) {
            val active = incoming.filter { it.active }
            prefs.setListSnapshot(revision, encodeList(active))
            return
        }
        val current = decodeList(prefs.getListJson()).associateBy { it.id }.toMutableMap()
        for (e in incoming) {
            if (!e.active) current.remove(e.id) else current[e.id] = e
        }
        prefs.setListSnapshot(revision, encodeList(current.values.toList()))
    }

    suspend fun saveRules(rules: List<SyncRule>) {
        val arr = JSONArray()
        for (r in rules) {
            arr.put(
                JSONObject()
                    .put("id", r.id)
                    .put("pattern", r.pattern)
                    .put("matchType", if (r.matchType == MatchType.EXACT) "exact" else "prefix")
                    .put("ttlPreset", r.ttlPreset)
                    .put("expiresAt", r.expiresAt)
                    .put("active", r.active),
            )
        }
        prefs.setRulesJson(arr.toString())
    }

    suspend fun loadSyncRules(): List<SyncRule> {
        val arr = JSONArray(prefs.getRulesJson())
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    SyncRule(
                        id = o.getString("id"),
                        pattern = o.getString("pattern"),
                        matchType = if (o.getString("matchType").equals("exact", true)) {
                            MatchType.EXACT
                        } else {
                            MatchType.PREFIX
                        },
                        ttlPreset = o.optString("ttlPreset", "permanent"),
                        expiresAt = if (o.isNull("expiresAt")) null else o.optString("expiresAt"),
                        active = o.optBoolean("active", true),
                    ),
                )
            }
        }
    }

    private fun encodeList(entries: List<SyncListEntry>): String {
        val arr = JSONArray()
        for (e in entries) {
            arr.put(
                JSONObject()
                    .put("id", e.id)
                    .put("pattern", e.pattern)
                    .put("active", e.active)
                    .put("revision", e.revision),
            )
        }
        return arr.toString()
    }

    private fun decodeList(json: String): List<SyncListEntry> {
        val arr = JSONArray(json)
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    SyncListEntry(
                        id = o.getString("id"),
                        pattern = o.getString("pattern"),
                        active = o.optBoolean("active", true),
                        revision = o.optLong("revision", 0),
                    ),
                )
            }
        }
    }

    private fun parseIso(iso: String): Long? = runCatching {
        Instant.parse(iso).toEpochMilli()
    }.getOrNull()
}
