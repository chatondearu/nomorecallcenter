package fr.chatondearu.nmcc.network

import fr.chatondearu.nmcc.matcher.MatchType
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class MeUser(
    val id: String,
    val email: String,
    val name: String?,
    val role: String?,
)

data class SyncListEntry(
    val id: String,
    val pattern: String,
    val active: Boolean,
    val revision: Long,
)

data class SyncRule(
    val id: String,
    val pattern: String,
    val matchType: MatchType,
    val ttlPreset: String,
    val expiresAt: String?,
    val active: Boolean,
)

data class StatsBundle(
    val scope: String,
    val screened7d: Int,
    val blocked7d: Int,
    val allowed7d: Int,
    val screened30d: Int,
    val blocked30d: Int,
    val allowed30d: Int,
    val recent: List<RecentEvent>,
)

data class RecentEvent(
    val id: String,
    val prefix: String?,
    val action: String,
    val occurredAt: String,
)

class ApiException(val status: Int, message: String) : Exception(message)

/**
 * Better Auth cookie session + REST helpers (doc/api.md).
 */
class ApiClient(
    private val cookieStore: MutableMap<String, MutableList<Cookie>> = mutableMapOf(),
) {
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                val host = url.host
                val bucket = cookieStore.getOrPut(host) { mutableListOf() }
                cookies.forEach { incoming ->
                    bucket.removeAll { it.name == incoming.name }
                    bucket.add(incoming)
                }
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url.host].orEmpty().filter { it.matches(url) }
            }
        })
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun clearCookies() {
        cookieStore.clear()
    }

    fun hasSessionCookie(baseUrl: String): Boolean {
        val host = baseUrl.toHttpUrlOrNull()?.host ?: return false
        return cookieStore[host].orEmpty().isNotEmpty()
    }

    fun signIn(baseUrl: String, email: String, password: String) {
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
            .toString()
            .toRequestBody(jsonMedia)
        val req = Request.Builder()
            .url("$baseUrl/api/auth/sign-in/email")
            .post(body)
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw ApiException(resp.code, resp.body?.string().orEmpty().ifBlank { "Sign-in failed" })
            }
        }
    }

    fun me(baseUrl: String): MeUser {
        val req = Request.Builder().url("$baseUrl/api/me").get().build()
        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw ApiException(resp.code, text.ifBlank { "Unauthorized" })
            val user = JSONObject(text).getJSONObject("user")
            return MeUser(
                id = user.getString("id"),
                email = user.getString("email"),
                name = if (user.isNull("name")) null else user.optString("name"),
                role = if (user.isNull("role")) null else user.optString("role"),
            )
        }
    }

    fun syncLists(baseUrl: String, since: Long): Pair<Long, List<SyncListEntry>> {
        val req = Request.Builder()
            .url("$baseUrl/api/lists/sync?since=$since")
            .get()
            .build()
        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw ApiException(resp.code, text)
            val root = JSONObject(text)
            val revision = root.getLong("revision")
            val entries = root.getJSONArray("entries")
            val list = buildList {
                for (i in 0 until entries.length()) {
                    val e = entries.getJSONObject(i)
                    add(
                        SyncListEntry(
                            id = e.getString("id"),
                            pattern = e.getString("pattern"),
                            active = e.getBoolean("active"),
                            revision = e.getLong("revision"),
                        ),
                    )
                }
            }
            return revision to list
        }
    }

    fun syncRules(baseUrl: String): List<SyncRule> {
        val req = Request.Builder().url("$baseUrl/api/rules/sync").get().build()
        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw ApiException(resp.code, text)
            val rules = JSONObject(text).getJSONArray("rules")
            return buildList {
                for (i in 0 until rules.length()) {
                    val r = rules.getJSONObject(i)
                    add(
                        SyncRule(
                            id = r.getString("id"),
                            pattern = r.getString("pattern"),
                            matchType = parseMatchType(r.getString("matchType")),
                            ttlPreset = r.getString("ttlPreset"),
                            expiresAt = if (r.isNull("expiresAt")) null else r.getString("expiresAt"),
                            active = r.optBoolean("active", true),
                        ),
                    )
                }
            }
        }
    }

    fun createRule(baseUrl: String, pattern: String, matchType: String, ttlPreset: String): SyncRule {
        val body = JSONObject()
            .put("pattern", pattern)
            .put("matchType", matchType)
            .put("ttlPreset", ttlPreset)
            .toString()
            .toRequestBody(jsonMedia)
        val req = Request.Builder().url("$baseUrl/api/rules").post(body).build()
        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw ApiException(resp.code, text)
            val r = JSONObject(text).getJSONObject("rule")
            return SyncRule(
                id = r.getString("id"),
                pattern = r.getString("pattern"),
                matchType = parseMatchType(r.getString("matchType")),
                ttlPreset = r.getString("ttlPreset"),
                expiresAt = if (r.isNull("expiresAt")) null else r.getString("expiresAt"),
                active = r.optBoolean("active", true),
            )
        }
    }

    fun propose(baseUrl: String, personalRuleId: String) {
        val body = JSONObject()
            .put("personalRuleId", personalRuleId)
            .toString()
            .toRequestBody(jsonMedia)
        val req = Request.Builder().url("$baseUrl/api/proposals").post(body).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw ApiException(resp.code, resp.body?.string().orEmpty())
            }
        }
    }

    fun postEvent(baseUrl: String, prefix: String?, hash: String?, action: String, occurredAt: String?) {
        val payload = JSONObject().put("action", action)
        if (!prefix.isNullOrBlank()) payload.put("prefix", prefix)
        if (!hash.isNullOrBlank()) payload.put("hash", hash)
        if (!occurredAt.isNullOrBlank()) payload.put("occurredAt", occurredAt)
        val req = Request.Builder()
            .url("$baseUrl/api/events")
            .post(payload.toString().toRequestBody(jsonMedia))
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw ApiException(resp.code, resp.body?.string().orEmpty())
            }
        }
    }

    fun statsMe(baseUrl: String): StatsBundle = parseStats(getJson(baseUrl, "/api/stats/me"))

    fun statsInstance(baseUrl: String): StatsBundle = parseStats(getJson(baseUrl, "/api/stats/instance"))

    private fun getJson(baseUrl: String, path: String): JSONObject {
        val req = Request.Builder().url("$baseUrl$path").get().build()
        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw ApiException(resp.code, text)
            return JSONObject(text)
        }
    }

    private fun parseStats(root: JSONObject): StatsBundle {
        val c7 = root.getJSONObject("counters").getJSONObject("7d")
        val c30 = root.getJSONObject("counters").getJSONObject("30d")
        val recentArr = root.optJSONArray("recent") ?: JSONArray()
        val recent = buildList {
            for (i in 0 until recentArr.length()) {
                val e = recentArr.getJSONObject(i)
                add(
                    RecentEvent(
                        id = e.getString("id"),
                        prefix = if (e.isNull("prefix")) null else e.optString("prefix"),
                        action = e.getString("action"),
                        occurredAt = e.getString("occurredAt"),
                    ),
                )
            }
        }
        return StatsBundle(
            scope = root.getString("scope"),
            screened7d = c7.optInt("screened"),
            blocked7d = c7.optInt("blocked"),
            allowed7d = c7.optInt("allowed"),
            screened30d = c30.optInt("screened"),
            blocked30d = c30.optInt("blocked"),
            allowed30d = c30.optInt("allowed"),
            recent = recent,
        )
    }

    private fun parseMatchType(raw: String): MatchType =
        if (raw.equals("exact", ignoreCase = true)) MatchType.EXACT else MatchType.PREFIX
}
