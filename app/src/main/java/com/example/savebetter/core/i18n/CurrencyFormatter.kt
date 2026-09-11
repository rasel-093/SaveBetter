package com.example.savebetter.core.i18n

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Presentation-layer currency formatter for Bangladesh Taka (BDT / ৳).
 *
 * Rules:
 * - Currency symbol is strictly BDT / ৳ (U+09F3) — ₹ is NEVER used.
 * - Money is stored as raw numeric values (Double, Long) and formatted here.
 * - Supports English (123) and Bangla (১২৩) numerals based on selected language.
 */
object CurrencyFormatter {

    const val CURRENCY_SYMBOL = "৳"

    private val englishFormatWithDecimals = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    private val englishFormatWhole = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))

    /**
     * Formats an amount with the ৳ symbol.
     *
     * Example (English): 1420.50 -> "৳1,420.50"
     * Example (Bangla): 1420.50 -> "৳১,৪২০.৫০"
     * Example (Negative): -50.0 -> "-৳50.00"
     */
    fun formatAmount(
        amount: Double,
        language: AppLanguage = AppLanguage.ENGLISH,
        includeDecimals: Boolean = true
    ): String {
        val isNegative = amount < 0
        val absAmount = abs(amount)

        val formatter = if (includeDecimals) englishFormatWithDecimals else englishFormatWhole
        val formattedNumber = formatter.format(absAmount)

        val baseString = "$CURRENCY_SYMBOL$formattedNumber"
        val signedString = if (isNegative) "-$baseString" else baseString

        return when (language) {
            AppLanguage.ENGLISH -> signedString
            AppLanguage.BANGLA -> NumeralConverter.toBanglaDigits(signedString)
        }
    }

    /**
     * Formats an amount explicitly showing +/- prefix.
     *
     * Example (Positive): 850.0 -> "+৳850.00"
     * Example (Negative): -120.0 -> "-৳120.00"
     * Example (Zero): 0.0 -> "৳0.00"
     */
    fun formatSignedAmount(
        amount: Double,
        language: AppLanguage = AppLanguage.ENGLISH,
        includeDecimals: Boolean = true
    ): String {
        return when {
            amount > 0 -> {
                val formatted = formatAmount(amount, language, includeDecimals)
                "+$formatted"
            }
            amount < 0 -> {
                formatAmount(amount, language, includeDecimals)
            }
            else -> {
                formatAmount(0.0, language, includeDecimals)
            }
        }
    }

    /**
     * Overload accepting integer / long amounts.
     */
    fun formatAmount(
        amount: Long,
        language: AppLanguage = AppLanguage.ENGLISH,
        includeDecimals: Boolean = false
    ): String = formatAmount(amount.toDouble(), language, includeDecimals)
}
