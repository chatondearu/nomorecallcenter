package fr.chatondearu.nmcc.matcher

/**
 * Pure call-rule matcher (prefix/exact + TTL). No Android deps — unit-testable on JVM.
 * Mirrors server semantics from @nmcc/db ttl helpers (ADR-007).
 */

enum class MatchType {
    EXACT,
    PREFIX,
}

enum class ScreenAction {
    /** Number matched a shared list entry or an active personal rule → silent reject. */
    BLOCK,
    /** No match → allow the call through. */
    ALLOW,
}

data class ListEntry(
    val pattern: String,
    val active: Boolean = true,
)

data class PersonalRule(
    val pattern: String,
    val matchType: MatchType,
    /** Null means permanent (ADR-007). */
    val expiresAtEpochMs: Long? = null,
)

data class ScreenDecision(
    val action: ScreenAction,
    /** Digits-only caller number used for matching, or null if unavailable. */
    val normalizedNumber: String?,
    /** Matched pattern when blocked; null when allowed. */
    val matchedPattern: String? = null,
)

/** Digits-only, 2–15 chars (same bounds as API normalizePattern). */
fun normalizeNumber(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val digits = raw.filter { it.isDigit() }
    if (digits.length < 2 || digits.length > 15) return null
    return digits
}

fun isRuleActive(expiresAtEpochMs: Long?, nowEpochMs: Long): Boolean {
    return expiresAtEpochMs == null || expiresAtEpochMs > nowEpochMs
}

fun matchesPattern(number: String, pattern: String, matchType: MatchType): Boolean {
    return when (matchType) {
        MatchType.EXACT -> number == pattern
        MatchType.PREFIX -> number.startsWith(pattern)
    }
}

/**
 * Decide whether to silently reject [rawNumber] given shared list + personal rules.
 * Shared list entries are treated as prefix matches (ARCEP / list sync contract).
 * Default product stance: match → BLOCK (silent reject); no match → ALLOW.
 */
fun decideScreen(
    rawNumber: String?,
    listEntries: Iterable<ListEntry>,
    personalRules: Iterable<PersonalRule>,
    nowEpochMs: Long = System.currentTimeMillis(),
): ScreenDecision {
    val number = normalizeNumber(rawNumber)
        ?: return ScreenDecision(ScreenAction.ALLOW, normalizedNumber = null)

    for (entry in listEntries) {
        if (!entry.active) continue
        val pattern = normalizeNumber(entry.pattern) ?: continue
        if (matchesPattern(number, pattern, MatchType.PREFIX)) {
            return ScreenDecision(ScreenAction.BLOCK, number, pattern)
        }
    }

    for (rule in personalRules) {
        if (!isRuleActive(rule.expiresAtEpochMs, nowEpochMs)) continue
        val pattern = normalizeNumber(rule.pattern) ?: continue
        if (matchesPattern(number, pattern, rule.matchType)) {
            return ScreenDecision(ScreenAction.BLOCK, number, pattern)
        }
    }

    return ScreenDecision(ScreenAction.ALLOW, number)
}
