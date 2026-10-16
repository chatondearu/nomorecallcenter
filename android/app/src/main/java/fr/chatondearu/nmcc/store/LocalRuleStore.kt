package fr.chatondearu.nmcc.store

import fr.chatondearu.nmcc.matcher.ListEntry
import fr.chatondearu.nmcc.matcher.MatchType
import fr.chatondearu.nmcc.matcher.PersonalRule
import fr.chatondearu.nmcc.matcher.RuleEffect
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
        return loadAllListEntries()
            .filter { it.active }
            .map { ListEntry(pattern = it.pattern, active = true) }
    }

    suspend fun loadAllListEntries(): List<SyncListEntry> = decodeList(prefs.getListJson())

    suspend fun setListEntryActive(id: String, active: Boolean) {
        val updated = loadAllListEntries().map { if (it.id == id) it.copy(active = active) else it }
        prefs.setListSnapshot(prefs.getListRevision(), encodeList(updated))
    }

    suspend fun loadPersonalRules(nowEpochMs: Long = System.currentTimeMillis()): List<PersonalRule> {
        return loadSyncRules()
            .filter { it.active }
            .map { rule ->
                PersonalRule(
                    pattern = rule.pattern,
                    matchType = rule.matchType,
                    expiresAtEpochMs = rule.expiresAt?.let { parseIso(it) },
                    effect = rule.effect,
                )
            }
            .filter { rule ->
                val expires = rule.expiresAtEpochMs
                expires == null || expires > nowEpochMs
            }
    }

    suspend fun setPersonalRuleActive(id: String, active: Boolean) {
        saveRules(loadSyncRules().map { if (it.id == id) it.copy(active = active) else it })
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
                    .put("active", r.active)
                    .put("effect", if (r.effect == RuleEffect.ALLOW) "allow" else "block"),
            )
        }
        prefs.setRulesJson(arr.toString())
    }

    /** Offline personal rule(s) — no API. TTL presets match server ADR-007. */
    suspend fun addLocalRule(pattern: String, matchType: String, ttlPreset: String, effect: String = "block") {
        addLocalRules(listOf(pattern), matchType, ttlPreset, effect)
    }

    suspend fun addLocalRules(
        patterns: List<String>,
        matchType: String,
        ttlPreset: String,
        effect: String = "block",
    ) {
        require(patterns.isNotEmpty()) { "Aucun motif" }
        val expiresAt = expiresIsoFor(ttlPreset)
        val type = if (matchType.equals("exact", true)) MatchType.EXACT else MatchType.PREFIX
        val ruleEffect = if (effect.equals("allow", true)) RuleEffect.ALLOW else RuleEffect.BLOCK
        val existing = loadSyncRules().toMutableList()
        val existingKeys = existing.map { ruleKey(it.pattern, it.matchType, it.effect) }.toHashSet()
        val now = System.currentTimeMillis()
        patterns.forEachIndexed { index, pattern ->
            val digits = pattern.filter { it.isDigit() }
            require(digits.length in 2..15) { "Le motif doit contenir 2 à 15 chiffres" }
            val key = ruleKey(digits, type, ruleEffect)
            if (key in existingKeys) return@forEachIndexed
            existing += SyncRule(
                id = "local-${now + index}",
                pattern = digits,
                matchType = type,
                ttlPreset = ttlPreset,
                expiresAt = expiresAt,
                active = true,
                effect = ruleEffect,
            )
            existingKeys += key
        }
        saveRules(existing)
    }

    private fun expiresIsoFor(ttlPreset: String): String? {
        val now = Instant.now()
        val until = when (ttlPreset) {
            "1h" -> now.plusSeconds(3600)
            "24h" -> now.plusSeconds(86_400)
            "7d" -> now.plusSeconds(7 * 86_400L)
            "30d" -> now.plusSeconds(30 * 86_400L)
            else -> return null
        }
        return until.toString()
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
                        effect = if (o.optString("effect", "block").equals("allow", true)) {
                            RuleEffect.ALLOW
                        } else {
                            RuleEffect.BLOCK
                        },
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

    private fun ruleKey(pattern: String, matchType: MatchType, effect: RuleEffect): String =
        "$pattern|$matchType|$effect"

    private fun parseIso(iso: String): Long? = runCatching {
        Instant.parse(iso).toEpochMilli()
    }.getOrNull()
}
