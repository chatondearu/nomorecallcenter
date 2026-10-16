package fr.chatondearu.nmcc.store

import android.content.Context
import fr.chatondearu.nmcc.prefs.AppPrefs
import org.json.JSONObject

/**
 * Seeds / upgrades the shared list from assets so screening works without a backend.
 * Replaces the snapshot when the asset revision is newer than the stored one.
 */
object OfflineBootstrap {
    private const val ASSET = "default_list.json"

    suspend fun ensureBundledList(context: Context, prefs: AppPrefs) {
        val raw = context.assets.open(ASSET).bufferedReader().use { it.readText() }
        val root = JSONObject(raw)
        val revision = root.optLong("revision", 1L)
        if (prefs.getListRevision() >= revision) return

        val entries = root.getJSONArray("entries")
        prefs.setListSnapshot(revision, entries.toString())
    }
}
