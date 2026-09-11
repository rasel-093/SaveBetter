package com.example.savebetter.core.domain.usecase.i18n

import com.example.savebetter.core.i18n.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [FormatCurrencyUseCase].
 */
class FormatCurrencyUseCaseTest {

    private val useCase = FormatCurrencyUseCase()

    @Test
    fun `formats unsigned amounts in English and Bangla`() {
        assertEquals("৳1,200.00", useCase(1200.0, AppLanguage.ENGLISH))
        assertEquals("৳১,২০০.৫০", useCase(1200.50, AppLanguage.BANGLA))
    }

    @Test
    fun `formats signed amounts when showSign is true`() {
        assertEquals("+৳500.00", useCase(500.0, AppLanguage.ENGLISH, showSign = true))
        assertEquals("-৳500.00", useCase(-500.0, AppLanguage.ENGLISH, showSign = true))
        assertEquals("+৳৫০০.০০", useCase(500.0, AppLanguage.BANGLA, showSign = true))
        assertEquals("-৳৫০০.০০", useCase(-500.0, AppLanguage.BANGLA, showSign = true))
    }

    @Test
    fun `respects includeDecimals parameter`() {
        assertEquals("৳3,500", useCase(3500.0, AppLanguage.ENGLISH, includeDecimals = false))
        assertEquals("৳৩,৫০০", useCase(3500.0, AppLanguage.BANGLA, includeDecimals = false))
    }
}
