package com.example.savebetter.core.i18n

/**
 * Bidirectional conversion between Western Arabic (0-9) and Eastern Nagari / Bengali numerals (০-৯).
 */
object NumeralConverter {

    private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    /**
     * Converts all ASCII digits (0-9) in the string to their Bengali equivalents (০-৯).
     * Non-digit characters (e.g. ',', '.', '৳', '-', '+') are preserved intact.
     */
    fun toBanglaDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val idx = ch - '0'
            if (idx in 0..9) {
                sb.append(banglaDigits[idx])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toBanglaDigits(number: Long): String = toBanglaDigits(number.toString())
    fun toBanglaDigits(number: Int): String = toBanglaDigits(number.toString())

    /**
     * Converts all Bengali digits (০-৯) in the string to standard ASCII digits (0-9).
     */
    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val idx = banglaDigits.indexOf(ch)
            if (idx != -1) {
                sb.append(englishDigits[idx])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}
