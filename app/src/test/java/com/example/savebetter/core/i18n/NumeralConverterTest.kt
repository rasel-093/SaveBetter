package com.example.savebetter.core.i18n

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [NumeralConverter] verifying bidirectional Western and Bengali numeral mapping.
 */
class NumeralConverterTest {

    @Test
    fun `toBanglaDigits converts standard digits correctly`() {
        val input = "0123456789"
        val expected = "০১২৩৪৫৬৭৮৯"
        assertEquals(expected, NumeralConverter.toBanglaDigits(input))
    }

    @Test
    fun `toBanglaDigits preserves symbols and punctuation`() {
        val input = "৳1,420.50"
        val expected = "৳১,৪২০.৫০"
        assertEquals(expected, NumeralConverter.toBanglaDigits(input))

        val negativeInput = "-৳50.00"
        val negativeExpected = "-৳৫০.০০"
        assertEquals(negativeExpected, NumeralConverter.toBanglaDigits(negativeInput))

        val positiveSigned = "+৳850.00"
        val positiveExpected = "+৳৮৫০.০০"
        assertEquals(positiveExpected, NumeralConverter.toBanglaDigits(positiveSigned))
    }

    @Test
    fun `toBanglaDigits handles numeric overloads`() {
        assertEquals("১০০", NumeralConverter.toBanglaDigits(100))
        assertEquals("৯৮৭৬৫৪৩২১০", NumeralConverter.toBanglaDigits(9876543210L))
    }

    @Test
    fun `toEnglishDigits converts Bengali digits back to Western digits`() {
        val input = "০১২৩৪৫৬৭৮৯"
        val expected = "0123456789"
        assertEquals(expected, NumeralConverter.toEnglishDigits(input))
    }

    @Test
    fun `toEnglishDigits preserves currency symbols and punctuation`() {
        val input = "৳১,৪২০.৫০"
        val expected = "৳1,420.50"
        assertEquals(expected, NumeralConverter.toEnglishDigits(input))
    }

    @Test
    fun `roundtrip conversion preserves original Western digits`() {
        val original = "123456.78"
        val bangla = NumeralConverter.toBanglaDigits(original)
        val english = NumeralConverter.toEnglishDigits(bangla)
        assertEquals(original, english)
    }
}
