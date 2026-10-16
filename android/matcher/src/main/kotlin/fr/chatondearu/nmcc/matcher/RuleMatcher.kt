package fr.chatondearu.nmcc.matcher

/**
 * Pure call-rule matcher (prefix/exact + TTL). No Android deps — unit-testable on JVM.
 * Mirrors server semantics from @nmcc/db ttl helpers (ADR-007).
 *
 * FR national numbers starting with 0 are canonicalized to E.164 without '+'
 * (0612345678 → 33612345678) so rules and inbound handles match either form.
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

/**
 * Digits-only, FR-aware canonical form (2–15 chars).
 * - strips non-digits
 * - drops leading international `00`
 * - maps national `0…` → `33…` (France-first product)
 */
fun normalizeNumber(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    var digits = raw.filter { it.isDigit() }
    if (digits.startsWith("00")) digits = digits.drop(2)
    if (digits.startsWith("0")) digits = "33" + digits.drop(1)
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
 * Expand a user-entered local pattern into national + international digit forms.
 *
 * For France ([countryCallingCode] `33`): entering `783`, `0783`, or `33783` yields
 * both `0783…` and `33783…` so rules cover either presentation without duplicate work.
 * Other countries: international form only (`[cc]` + national-significant digits).
 */
fun expandCountryPrefixPatterns(countryCallingCode: String, rawLocal: String): List<String> {
    val cc = countryCallingCode.filter { it.isDigit() }
    require(cc.isNotEmpty() && cc.length <= 3) { "Invalid country calling code" }

    var local = rawLocal.filter { it.isDigit() }
    require(local.length >= 2) { "Pattern must be at least 2 digits" }

    if (local.startsWith("00")) local = local.drop(2)
    if (local.startsWith(cc)) local = local.drop(cc.length)

    return when (cc) {
        "33" -> {
            val national = if (local.startsWith("0")) local else "0$local"
            val international = cc + national.drop(1)
            listOf(national, international)
                .map { it.take(15) }
                .filter { it.length in 2..15 }
                .distinct()
        }
        else -> {
            val significant = if (local.startsWith("0")) local.drop(1) else local
            val international = (cc + significant).take(15)
            require(international.length in 2..15) { "Pattern must be 2–15 digits" }
            listOf(international)
        }
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
