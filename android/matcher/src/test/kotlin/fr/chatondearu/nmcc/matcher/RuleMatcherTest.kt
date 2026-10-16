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
        assertEquals("3389200", normalizeNumber("0892-00"))
        assertNull(normalizeNumber("1"))
        assertNull(normalizeNumber(""))
        assertNull(normalizeNumber(null))
        assertNull(normalizeNumber("1234567890123456")) // 16 digits
    }

    @Test
    fun normalizeNumber_frNationalAndInternationalEquivalent() {
        assertEquals("33612345678", normalizeNumber("0612345678"))
        assertEquals("33612345678", normalizeNumber("+33612345678"))
        assertEquals("33612345678", normalizeNumber("0033612345678"))
        assertEquals("33612", normalizeNumber("0612"))
        assertEquals("33612", normalizeNumber("33612"))
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
        assertEquals("3389200", decision.matchedPattern)
    }

    @Test
    fun prefixMatch_nationalRuleBlocksInternationalCaller() {
        val decision = decideScreen(
            rawNumber = "+33612345678",
            listEntries = emptyList(),
            personalRules = listOf(
                PersonalRule("0612345678", MatchType.PREFIX, expiresAtEpochMs = null),
            ),
            nowEpochMs = now,
        )
        assertEquals(ScreenAction.BLOCK, decision.action)
        assertEquals("33612345678", decision.matchedPattern)
    }

    @Test
    fun prefixMatch_internationalRuleBlocksNationalCaller() {
        val decision = decideScreen(
            rawNumber = "0612345678",
            listEntries = emptyList(),
            personalRules = listOf(
                PersonalRule("33612345678", MatchType.EXACT, expiresAtEpochMs = null),
            ),
            nowEpochMs = now,
        )
        assertEquals(ScreenAction.BLOCK, decision.action)
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
        assertEquals("33612345678", decision.matchedPattern)
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
        // 0102030405 → 33102030405; 06 → 336 — no match; 0892 → 33892 — no match
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

    @Test
    fun expandCountryPrefixPatterns_franceBothForms() {
        assertEquals(listOf("0783", "33783"), expandCountryPrefixPatterns("33", "0783"))
        assertEquals(listOf("0783", "33783"), expandCountryPrefixPatterns("33", "783"))
        assertEquals(listOf("0783", "33783"), expandCountryPrefixPatterns("33", "33783"))
        assertEquals(listOf("0783", "33783"), expandCountryPrefixPatterns("+33", "0783"))
    }

    @Test
    fun allowWinsOverSharedListAndPersonalBlock() {
        val decision = decideScreen(
            rawNumber = "0612345678",
            listEntries = listOf(ListEntry("336")),
            personalRules = listOf(
                PersonalRule("0612", MatchType.PREFIX, effect = RuleEffect.BLOCK),
                PersonalRule("0612345678", MatchType.EXACT, effect = RuleEffect.ALLOW),
            ),
            nowEpochMs = now,
            contactsOnly = true,
            isDeviceContact = false,
        )
        assertEquals(ScreenAction.ALLOW, decision.action)
        assertEquals("33612345678", decision.matchedPattern)
    }

    @Test
    fun expiredAllow_doesNotWin() {
        val decision = decideScreen(
            rawNumber = "0612345678",
            listEntries = listOf(ListEntry("336")),
            personalRules = listOf(
                PersonalRule("0612345678", MatchType.EXACT, expiresAtEpochMs = now - 1, effect = RuleEffect.ALLOW),
            ),
            nowEpochMs = now,
        )
        assertEquals(ScreenAction.BLOCK, decision.action)
    }

    @Test
    fun contactsOnly_blocksUnknownUnlessAllow() {
        val blocked = decideScreen(
            rawNumber = "0102030405",
            listEntries = emptyList(),
            personalRules = emptyList(),
            nowEpochMs = now,
            contactsOnly = true,
            isDeviceContact = false,
        )
        assertEquals(ScreenAction.BLOCK, blocked.action)
        assertNull(blocked.matchedPattern)

        val contact = decideScreen(
            rawNumber = "0102030405",
            listEntries = emptyList(),
            personalRules = emptyList(),
            nowEpochMs = now,
            contactsOnly = true,
            isDeviceContact = true,
        )
        assertEquals(ScreenAction.ALLOW, contact.action)
    }

    @Test
    fun hiddenNumber_blocksOnlyWhenSettingOn() {
        val open = decideScreen(
            rawNumber = null,
            listEntries = listOf(ListEntry("0892")),
            personalRules = emptyList(),
            nowEpochMs = now,
            blockHiddenNumbers = false,
        )
        assertEquals(ScreenAction.ALLOW, open.action)

        val hidden = decideScreen(
            rawNumber = "",
            listEntries = emptyList(),
            personalRules = emptyList(),
            nowEpochMs = now,
            blockHiddenNumbers = true,
        )
        assertEquals(ScreenAction.BLOCK, hidden.action)
        assertNull(hidden.normalizedNumber)

        val contactsOnlyHidden = decideScreen(
            rawNumber = null,
            listEntries = emptyList(),
            personalRules = emptyList(),
            nowEpochMs = now,
            contactsOnly = true,
        )
        assertEquals(ScreenAction.BLOCK, contactsOnlyHidden.action)
    }

    @Test
    fun expandCountryPrefixPatterns_otherCountryInternationalOnly() {
        assertEquals(listOf("324712"), expandCountryPrefixPatterns("32", "04712"))
        assertEquals(listOf("324712"), expandCountryPrefixPatterns("32", "4712"))
    }
}
