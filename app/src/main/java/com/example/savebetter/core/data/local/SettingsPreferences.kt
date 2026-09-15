package com.example.savebetter.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.savebetter.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages persisting and observing application display and notification settings from DataStore.
 */
@Singleton
class SettingsPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val code = preferences[PreferenceKeys.THEME_MODE]
        ThemeMode.fromCode(code)
    }

    val vibrationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.VIBRATION_ENABLED] ?: true
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    val notifyWeeklyWarning: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.NOTIFY_WEEKLY_WARNING] ?: true
    }

    val notifyMonthlyWarning: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.NOTIFY_MONTHLY_WARNING] ?: true
    }

    val notifyReconciliation: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.NOTIFY_RECONCILIATION] ?: true
    }

    val notifyWeeklyBudgetReminder: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.NOTIFY_WEEKLY_BUDGET_REMINDER] ?: true
    }

    val notifyMonthlyBudgetReminder: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.NOTIFY_MONTHLY_BUDGET_REMINDER] ?: true
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = mode.code
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setNotifyWeeklyWarning(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFY_WEEKLY_WARNING] = enabled
        }
    }

    suspend fun setNotifyMonthlyWarning(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFY_MONTHLY_WARNING] = enabled
        }
    }

    suspend fun setNotifyReconciliation(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFY_RECONCILIATION] = enabled
        }
    }

    suspend fun setNotifyWeeklyBudgetReminder(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFY_WEEKLY_BUDGET_REMINDER] = enabled
        }
    }

    suspend fun setNotifyMonthlyBudgetReminder(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFY_MONTHLY_BUDGET_REMINDER] = enabled
        }
    }

    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

