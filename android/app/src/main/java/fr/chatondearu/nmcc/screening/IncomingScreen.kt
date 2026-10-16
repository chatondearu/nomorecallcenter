package fr.chatondearu.nmcc.screening

import android.content.Context
import fr.chatondearu.nmcc.matcher.ScreenDecision
import fr.chatondearu.nmcc.matcher.decideScreen
import fr.chatondearu.nmcc.matcher.normalizeNumber
import fr.chatondearu.nmcc.prefs.AppPrefs
import fr.chatondearu.nmcc.store.LocalRuleStore

/**
 * Device prefs + local contact check, then the pure matcher.
 * Contacts-only is ignored when READ_CONTACTS is missing so a denied permission does not block everyone.
 */
suspend fun decideIncoming(
    context: Context,
    prefs: AppPrefs,
    store: LocalRuleStore,
    rawNumber: String?,
    nowEpochMs: Long = System.currentTimeMillis(),
): ScreenDecision {
    val contactsOnly = prefs.getContactsOnly() && hasContactsPermission(context)
    val number = normalizeNumber(rawNumber)
    val isContact = number != null && contactsOnly && numberIsDeviceContact(context, number)
    return decideScreen(
        rawNumber = rawNumber,
        listEntries = store.loadListEntries(),
        personalRules = store.loadPersonalRules(nowEpochMs),
        nowEpochMs = nowEpochMs,
        blockHiddenNumbers = prefs.getBlockHiddenNumbers(),
        contactsOnly = contactsOnly,
        isDeviceContact = isContact,
    )
}
