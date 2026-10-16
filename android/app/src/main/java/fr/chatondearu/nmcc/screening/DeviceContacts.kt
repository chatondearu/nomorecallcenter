package fr.chatondearu.nmcc.screening

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

fun hasContactsPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
        PackageManager.PERMISSION_GRANTED

/**
 * Local lookup only. The contact list is never uploaded.
 * [normalized] is digits in the matcher canonical form (FR 33…).
 */
fun numberIsDeviceContact(context: Context, normalized: String): Boolean {
    if (!hasContactsPermission(context)) return false
    val candidates = buildList {
        add(normalized)
        add("+$normalized")
        if (normalized.startsWith("33") && normalized.length > 2) {
            add("0" + normalized.drop(2))
        }
    }
    val projection = arrayOf(ContactsContract.PhoneLookup._ID)
    for (candidate in candidates.distinct()) {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(candidate),
        )
        val hit = runCatching {
            context.contentResolver.query(uri, projection, null, null, null)?.use { it.moveToFirst() } == true
        }.getOrDefault(false)
        if (hit) return true
    }
    return false
}
