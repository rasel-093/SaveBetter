package com.example.savebetter.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore preference keys used across the application.
 *
 * All preference keys are defined here to provide a single source of truth
 * and prevent key-name collisions.
 *
 * Preference values are accessed through [UserPreferencesDataStore].
 */
object PreferenceKeys {
    /** IETF language tag for the user's selected language, e.g. "en" or "bn". */
    val LANGUAGE_TAG       = stringPreferencesKey("language_tag")

    /** Theme preference: "system", "light", or "dark". */
    val THEME_MODE         = stringPreferencesKey("theme_mode")

    /** Whether haptic feedback (vibration) is enabled. */
    val VIBRATION_ENABLED  = booleanPreferencesKey("vibration_enabled")

    /** Master notifications toggle. */
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

    /** Weekly target warning notification. */
    val NOTIFY_WEEKLY_WARNING  = booleanPreferencesKey("notify_weekly_warning")

    /** Monthly target warning notification. */
    val NOTIFY_MONTHLY_WARNING = booleanPreferencesKey("notify_monthly_warning")

    /** Month-end reconciliation reminder. */
    val NOTIFY_RECONCILIATION  = booleanPreferencesKey("notify_reconciliation")

    /** First day of week budget setup reminder (Monday). */
    val NOTIFY_WEEKLY_BUDGET_REMINDER = booleanPreferencesKey("notify_weekly_budget_reminder")

    /** First day of month budget setup reminder (1st of month). */
    val NOTIFY_MONTHLY_BUDGET_REMINDER = booleanPreferencesKey("notify_monthly_budget_reminder")
}

/** Extension property to create/access the app-level DataStore. */
val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)
