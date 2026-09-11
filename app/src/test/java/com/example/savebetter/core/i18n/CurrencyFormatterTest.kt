package com.example.savebetter.core.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [CurrencyFormatter] verifying strict BDT (৳) presentation and numeral rendering.
 */
class CurrencyFormatterTest {

    @Test
    fun `formatAmount in English formats standard amount with commas and decimals`() {
        val result = CurrencyFormatter.formatAmount(1420.50, AppLanguage.ENGLISH)
        assertEquals("৳1,420.50", result)
    }

    @Test
    fun `formatAmount in Bangla formats amount with Bengali digits`() {
        val result = CurrencyFormatter.formatAmount(1420.50, AppLanguage.BANGLA)
        assertEquals("৳১,৪২০.৫০", result)
    }

    @Test
    fun `formatAmount without decimals formats whole numbers correctly`() {
        val enResult = CurrencyFormatter.formatAmount(3500.0, AppLanguage.ENGLISH, includeDecimals = false)
        assertEquals("৳3,500", enResult)

        val bnResult = CurrencyFormatter.formatAmount(3500.0, AppLanguage.BANGLA, includeDecimals = false)
        assertEquals("৳৩,৫০০", bnResult)
    }

    @Test
    fun `formatAmount handles negative amounts correctly`() {
        val enNegative = CurrencyFormatter.formatAmount(-50.0, AppLanguage.ENGLISH)
        assertEquals("-৳50.00", enNegative)

        val bnNegative = CurrencyFormatter.formatAmount(-50.0, AppLanguage.BANGLA)
        assertEquals("-৳৫০.০০", bnNegative)
    }

    @Test
    fun `formatAmount handles zero correctly`() {
        val enZero = CurrencyFormatter.formatAmount(0.0, AppLanguage.ENGLISH)
        assertEquals("৳0.00", enZero)

        val bnZero = CurrencyFormatter.formatAmount(0.0, AppLanguage.BANGLA)
        assertEquals("৳০.০০", bnZero)
    }

    @Test
    fun `formatSignedAmount prefixes positive with plus and negative with minus`() {
        val enPositive = CurrencyFormatter.formatSignedAmount(850.0, AppLanguage.ENGLISH)
        assertEquals("+৳850.00", enPositive)

        val bnPositive = CurrencyFormatter.formatSignedAmount(850.0, AppLanguage.BANGLA)
        assertEquals("+৳৮৫০.০০", bnPositive)

        val enNegative = CurrencyFormatter.formatSignedAmount(-120.0, AppLanguage.ENGLISH)
        assertEquals("-৳120.00", enNegative)

        val bnNegative = CurrencyFormatter.formatSignedAmount(-120.0, AppLanguage.BANGLA)
        assertEquals("-৳১২০.০০", bnNegative)

        val enZero = CurrencyFormatter.formatSignedAmount(0.0, AppLanguage.ENGLISH)
        assertEquals("৳0.00", enZero)

        val bnZero = CurrencyFormatter.formatSignedAmount(0.0, AppLanguage.BANGLA)
        assertEquals("৳০.০০", bnZero)
    }

    @Test
    fun `formatAmount strictly uses BDT symbol and never rupee symbol`() {
        val sample = CurrencyFormatter.formatAmount(100.0, AppLanguage.ENGLISH)
        assertTrue("Must contain BDT (৳)", sample.contains("৳"))
        assertFalse("Must never contain rupee (₹)", sample.contains("₹"))
        assertFalse("Must never contain dollar ($)", sample.contains("$"))
    }

    @Test
    fun `Long overload formats correctly`() {
        val enResult = CurrencyFormatter.formatAmount(25000L, AppLanguage.ENGLISH, includeDecimals = false)
        assertEquals("৳25,000", enResult)

        val bnResult = CurrencyFormatter.formatAmount(25000L, AppLanguage.BANGLA, includeDecimals = false)
        assertEquals("৳২৫,০০০", bnResult)
    }
}
