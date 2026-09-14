package com.example.savebetter.feature.settings

import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.ThemeMode
import com.example.savebetter.core.i18n.AppLanguage

/**
 * UI state for Settings screen (Screen 09).
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userEmail: String = "",
    val monthlySalaryMinor: Long = 0L,
    val weeklyTargetAmountMinor: Long = 0L,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val vibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val notifyWeeklyWarning: Boolean = true,
    val notifyMonthlyWarning: Boolean = true,
    val notifyReconciliation: Boolean = true,
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val isSyncing: Boolean = false,
    val syncStatus: com.example.savebetter.core.designsystem.component.SyncStatus = com.example.savebetter.core.designsystem.component.SyncStatus.Synced,
    val syncMessage: String? = null,
    val categories: List<Category> = emptyList(),
    val showEditProfileDialog: Boolean = false,
    val showThemeDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val showCategoriesDialog: Boolean = false,
    val showWeeklyTargetDialog: Boolean = false,
    val showLogoutConfirmDialog: Boolean = false,
    val showDeleteAccountConfirmDialog: Boolean = false,
    val showReauthDialog: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val reauthError: String? = null,
    val userMessage: String? = null
)

