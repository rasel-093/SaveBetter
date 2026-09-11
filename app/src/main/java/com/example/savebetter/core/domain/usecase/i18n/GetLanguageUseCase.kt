package com.example.savebetter.core.domain.usecase.i18n

import com.example.savebetter.core.data.local.LanguagePreferences
import com.example.savebetter.core.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to observe the current active application language.
 */
class GetLanguageUseCase @Inject constructor(
    private val languagePreferences: LanguagePreferences
) {
    operator fun invoke(): Flow<AppLanguage> = languagePreferences.language
}
