package com.example.savebetter.core.i18n

/**
 * Supported application languages.
 */
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    ENGLISH("en", "English", "English"),
    BANGLA("bn", "Bangla", "বাংলা");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
