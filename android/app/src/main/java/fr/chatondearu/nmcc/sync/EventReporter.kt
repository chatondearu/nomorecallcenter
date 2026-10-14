package fr.chatondearu.nmcc.sync

import fr.chatondearu.nmcc.matcher.ScreenAction
import fr.chatondearu.nmcc.matcher.ScreenDecision
import fr.chatondearu.nmcc.matcher.normalizeNumber
import fr.chatondearu.nmcc.network.ApiClient
import fr.chatondearu.nmcc.prefs.AppPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.time.Instant

/**
 * Best-effort call-event ingest with a small offline queue (ADR-001: prefix/hash only).
 */
class EventReporter(
    private val api: ApiClient,
    private val prefs: AppPrefs,
    private val scope: CoroutineScope,
) {
    fun enqueueAfterScreen(decision: ScreenDecision) {
        scope.launch(Dispatchers.IO) {
            val action = when (decision.action) {
                ScreenAction.BLOCK -> "blocked"
                ScreenAction.ALLOW -> "allowed"
            }
            val number = decision.normalizedNumber
            val prefix = number?.take(8)?.takeIf { it.length >= 2 }
            val hash = number?.let { sha256Hex(it) }
            appendQueue(prefix, hash, action, Instant.now().toString())
            flush()
        }
    }

    suspend fun flush() {
        val base = prefs.getBaseUrl()
        if (base.isBlank() || !api.hasSessionCookie(base)) return

        val remaining = JSONArray()
        val queue = JSONArray(prefs.getEventQueueJson())
        for (i in 0 until queue.length()) {
            val item = queue.getJSONObject(i)
            try {
                api.postEvent(
                    baseUrl = base,
                    prefix = if (item.isNull("prefix")) null else item.optString("prefix"),
                    hash = if (item.isNull("hash")) null else item.optString("hash"),
                    action = item.getString("action"),
                    occurredAt = if (item.isNull("occurredAt")) null else item.optString("occurredAt"),
                )
            } catch (_: Exception) {
                remaining.put(item)
            }
        }
        prefs.setEventQueueJson(remaining.toString())
    }

    private suspend fun appendQueue(prefix: String?, hash: String?, action: String, occurredAt: String) {
        val arr = JSONArray(prefs.getEventQueueJson())
        arr.put(
            JSONObject()
                .put("prefix", prefix)
                .put("hash", hash)
                .put("action", action)
                .put("occurredAt", occurredAt),
        )
        // Cap queue to avoid unbounded growth offline
        while (arr.length() > 200) {
            arr.remove(0)
        }
        prefs.setEventQueueJson(arr.toString())
    }

    private fun sha256Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}

/** Normalize for event prefix helper (re-export style). */
fun eventPrefixFromRaw(raw: String?): String? = normalizeNumber(raw)?.take(8)
