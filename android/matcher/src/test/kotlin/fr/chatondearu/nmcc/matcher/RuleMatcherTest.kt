package fr.chatondearu.nmcc.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RuleMatcherTest {

    private val now = 1_700_000_000_000L

    @Test
    fun normalizeNumber_stripsNonDigitsAndBounds() {
        assertEquals("33612", normalizeNumber("+33 6 12"))
        assertEquals("089200", normalizeNumber("0892-00"))
        assertNull(normalizeNumber("1"))
        assertNull(normalizeNumber(""))
        assertNull(normalizeNumber(null))
        assertNull(normalizeNumber("1234567890123456")) // 16 digits
    }

    @Test
    fun isRuleActive_permanentAndTtl() {
        assertTrue(isRuleActive(null, now))
        assertTrue(isRuleActive(now + 1, now))
        assertFalse(isRuleActive(now, now))
        assertFalse(isRuleActive(now - 1, now))
    }

    @Test
    fun prefixMatch_blocksSharedList() {
        val decision = decideScreen(
            rawNumber = "0892001234",
            listEntries = listOf(ListEntry("089200")),
            personalRules = emptyList(),
            nowEpochMs = now,
        )
        assertEquals(ScreenAction.BLOCK, decision.action)
        assertEquals("089200", decision.matchedPattern)
    }

    @Test
    fun exactMatch_personalRule() {
        val decision = decideScreen(
            rawNumber = "0612345678",
            listEntries = emptyList(),
            personalRules = listOf(
                PersonalRule("0612345678", MatchType.EXACT, expiresAtEpochMs = null),
            ),
            nowEpochMs = now,
        )
        assertEquals(ScreenAction.BLOCK, decision.action)
        assertEquals("0612345678", decision.matchedPattern)
    }

    @Test
    fun expiredPersonalRule_doesNotBlock() {
        val decision = decideScreen(
            rawNumber = "0612345678",
            listEntries = emptyList(),
            personalRules = listOf(
                PersonalRule("0612", MatchType.PREFIX, expiresAtEpochMs = now - 1),
            ),
            nowEpochMs = now,
        )
        assertEquals(ScreenAction.ALLOW, decision.action)
        assertNull(decision.matchedPattern)
    }

    @Test
    fun inactiveListEntry_ignored() {
        val decision = decideScreen(
            rawNumber = "0892001234",
            listEntries = listOf(ListEntry("089200", active = false)),
            personalRules = emptyList(),
            nowEpochMs = now,
        )
        assertEquals(ScreenAction.ALLOW, decision.action)
    }

    @Test
    fun noMatch_allows() {
        val decision = decideScreen(
            rawNumber = "0102030405",
            listEntries = listOf(ListEntry("0892")),
            personalRules = listOf(
                PersonalRule("06", MatchType.PREFIX, expiresAtEpochMs = null),
            ),
            nowEpochMs = now,
        )
        assertEquals(ScreenAction.ALLOW, decision.action)
    }

    @Test
    fun missingNumber_allows() {
        val decision = decideScreen(
            rawNumber = null,
            listEntries = listOf(ListEntry("0892")),
            personalRules = emptyList(),
            nowEpochMs = now,
        )
        assertEquals(ScreenAction.ALLOW, decision.action)
        assertNull(decision.normalizedNumber)
    }
}
