package com.example.savebetter.core.i18n

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [AppLanguage] enum.
 */
class AppLanguageTest {

    @Test
    fun `fromCode resolves known codes correctly`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.BANGLA, AppLanguage.fromCode("bn"))
    }

    @Test
    fun `fromCode falls back to ENGLISH for unknown or null codes`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("fr"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode(null))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode(""))
    }

    @Test
    fun `language codes match standard ISO codes`() {
        assertEquals("en", AppLanguage.ENGLISH.code)
        assertEquals("bn", AppLanguage.BANGLA.code)
    }
}
