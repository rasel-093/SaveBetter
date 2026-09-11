package com.example.savebetter.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.savebetter.core.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages persisting and retrieving user language preference from DataStore.
 */
@Singleton
class LanguagePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    /**
     * Flow of the current application language, defaulting to [AppLanguage.ENGLISH].
     */
    val language: Flow<AppLanguage> = dataStore.data.map { preferences ->
        val tag = preferences[PreferenceKeys.LANGUAGE_TAG]
        AppLanguage.fromCode(tag)
    }

    /**
     * Persists the selected language code ("en" or "bn") to DataStore.
     */
    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.LANGUAGE_TAG] = language.code
        }
    }
}
