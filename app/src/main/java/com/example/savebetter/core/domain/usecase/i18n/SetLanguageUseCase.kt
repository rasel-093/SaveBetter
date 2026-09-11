package com.example.savebetter.core.domain.usecase.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.savebetter.core.data.local.LanguagePreferences
import com.example.savebetter.core.i18n.AppLanguage
import javax.inject.Inject

/**
 * Use case to change application language, updating both DataStore and
 * Android per-app language settings with backward compatibility for API 26–32.
 */
class SetLanguageUseCase @Inject constructor(
    private val languagePreferences: LanguagePreferences
) {
    suspend operator fun invoke(language: AppLanguage) {
        languagePreferences.setLanguage(language)
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}
