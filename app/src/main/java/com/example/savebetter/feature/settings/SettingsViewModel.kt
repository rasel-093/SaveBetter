package com.example.savebetter.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebetter.R
import com.example.savebetter.core.auth.model.RecentLoginRequiredException
import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.data.local.LanguagePreferences
import com.example.savebetter.core.data.local.SettingsPreferences
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.ThemeMode
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklyTarget
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.DebtCreditRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import com.example.savebetter.core.domain.repository.UserProfileRepository
import com.example.savebetter.core.domain.usecase.auth.DeleteAccountUseCase
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.notification.ReconciliationReminderScheduler
import com.example.savebetter.core.designsystem.component.SyncStatus
import com.example.savebetter.core.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val settingsPreferences: SettingsPreferences,
    private val languagePreferences: LanguagePreferences,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val targetRepository: TargetRepository,
    private val debtCreditRepository: DebtCreditRepository,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val syncManager: SyncManager? = null
) : ViewModel() {

    private val activeUserId = MutableStateFlow<String?>(null)

    data class DialogStates(
        val showEditProfile: Boolean = false,
        val showTheme: Boolean = false,
        val showLanguage: Boolean = false,
        val showCategories: Boolean = false,
        val showLogout: Boolean = false,
        val showDeleteAccount: Boolean = false,
        val showReauthDialog: Boolean = false,
        val isDeletingAccount: Boolean = false,
        val reauthError: String? = null,
        val isSyncing: Boolean = false,
        val syncMessage: String? = null,
        val userMessage: String? = null
    )

    private val dialogStates = MutableStateFlow(DialogStates())

    init {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                if (user != null) {
                    activeUserId.value = user.id
                }
            }
        }
    }

    fun initUser(userId: String) {
        activeUserId.value = userId
    }

    private val userProfileFlow = activeUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(null) else userProfileRepository.observeUserProfile(userId)
    }

    private val categoriesFlow = activeUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList()) else categoryRepository.observeCategories(userId)
    }

    private data class UserAndCategoriesData(
        val userId: String?,
        val profile: UserProfile?,
        val categories: List<Category>
    )

    private val userAndCategoriesFlow = combine(
        activeUserId,
        userProfileFlow,
        categoriesFlow
    ) { userId, profile, categories ->
        UserAndCategoriesData(userId, profile, categories)
    }

    private data class PreferencesGroup(
        val theme: ThemeMode,
        val vibration: Boolean,
        val notifyAll: Boolean,
        val notifyWeekly: Boolean,
        val notifyMonthly: Boolean
    )

    private val preferencesFlow = combine(
        settingsPreferences.themeMode,
        settingsPreferences.vibrationEnabled,
        settingsPreferences.notificationsEnabled,
        settingsPreferences.notifyWeeklyWarning,
        settingsPreferences.notifyMonthlyWarning
    ) { theme, vibration, notifyAll, notifyWeekly, notifyMonthly ->
        PreferencesGroup(theme, vibration, notifyAll, notifyWeekly, notifyMonthly)
    }

    private data class MiscPreferences(
        val notifyReconciliation: Boolean,
        val notifyWeeklyBudgetReminder: Boolean,
        val notifyMonthlyBudgetReminder: Boolean,
        val language: AppLanguage
    )

    private val miscPreferencesFlow = combine(
        settingsPreferences.notifyReconciliation,
        settingsPreferences.notifyWeeklyBudgetReminder,
        settingsPreferences.notifyMonthlyBudgetReminder,
        languagePreferences.language
    ) { notifyReconciliation, notifyWeeklyReminder, notifyMonthlyReminder, language ->
        MiscPreferences(notifyReconciliation, notifyWeeklyReminder, notifyMonthlyReminder, language)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        userAndCategoriesFlow,
        preferencesFlow,
        miscPreferencesFlow,
        dialogStates
    ) { userData, pref, misc, dialogs ->
        SettingsUiState(
            isLoading = userData.userId == null,
            userName = userData.profile?.name ?: "",
            userEmail = userData.profile?.email ?: "",
            monthlySalaryMinor = userData.profile?.monthlySalaryMinor ?: 0L,
            themeMode = pref.theme,
            vibrationEnabled = pref.vibration,
            notificationsEnabled = pref.notifyAll,
            notifyWeeklyWarning = pref.notifyWeekly,
            notifyMonthlyWarning = pref.notifyMonthly,
            notifyReconciliation = misc.notifyReconciliation,
            notifyWeeklyBudgetReminder = misc.notifyWeeklyBudgetReminder,
            notifyMonthlyBudgetReminder = misc.notifyMonthlyBudgetReminder,
            currentLanguage = misc.language,
            isSyncing = dialogs.isSyncing,
            syncMessage = dialogs.syncMessage,
            categories = userData.categories,
            showEditProfileDialog = dialogs.showEditProfile,
            showThemeDialog = dialogs.showTheme,
            showLanguageDialog = dialogs.showLanguage,
            showCategoriesDialog = dialogs.showCategories,
            showLogoutConfirmDialog = dialogs.showLogout,
            showDeleteAccountConfirmDialog = dialogs.showDeleteAccount,
            showReauthDialog = dialogs.showReauthDialog,
            isDeletingAccount = dialogs.isDeletingAccount,
            reauthError = dialogs.reauthError,
            userMessage = dialogs.userMessage
        )
    }.combine(syncManager?.syncStatus ?: flowOf(SyncStatus.Synced)) { state, liveStatus ->
        state.copy(syncStatus = liveStatus, isSyncing = state.isSyncing || liveStatus == SyncStatus.Syncing)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(isLoading = true)
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsPreferences.setThemeMode(mode)
            dialogStates.update { it.copy(showTheme = false) }
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setVibrationEnabled(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setNotificationsEnabled(enabled)
            if (enabled) {
                // If master enabled, schedule reminder services
                ReconciliationReminderScheduler.scheduleMonthEndReminder(context)
                com.example.savebetter.core.notification.BudgetReminderScheduler.scheduleWeeklyBudgetReminder(context)
                com.example.savebetter.core.notification.BudgetReminderScheduler.scheduleMonthlyBudgetReminder(context)
            } else {
                ReconciliationReminderScheduler.cancelReminder(context)
                com.example.savebetter.core.notification.BudgetReminderScheduler.cancelWeeklyReminder(context)
                com.example.savebetter.core.notification.BudgetReminderScheduler.cancelMonthlyReminder(context)
            }
        }
    }

    fun setNotifyWeeklyWarning(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setNotifyWeeklyWarning(enabled)
        }
    }

    fun setNotifyMonthlyWarning(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setNotifyMonthlyWarning(enabled)
        }
    }

    fun setNotifyReconciliation(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setNotifyReconciliation(enabled)
            if (enabled) {
                ReconciliationReminderScheduler.scheduleMonthEndReminder(context)
            } else {
                ReconciliationReminderScheduler.cancelReminder(context)
            }
        }
    }

    fun setNotifyWeeklyBudgetReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setNotifyWeeklyBudgetReminder(enabled)
            if (enabled) {
                com.example.savebetter.core.notification.BudgetReminderScheduler.scheduleWeeklyBudgetReminder(context)
            } else {
                com.example.savebetter.core.notification.BudgetReminderScheduler.cancelWeeklyReminder(context)
            }
        }
    }

    fun setNotifyMonthlyBudgetReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setNotifyMonthlyBudgetReminder(enabled)
            if (enabled) {
                com.example.savebetter.core.notification.BudgetReminderScheduler.scheduleMonthlyBudgetReminder(context)
            } else {
                com.example.savebetter.core.notification.BudgetReminderScheduler.cancelMonthlyReminder(context)
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            languagePreferences.setLanguage(language)
            dialogStates.update { it.copy(showLanguage = false) }
        }
    }

    fun updateProfile(name: String, salaryMinor: Long) {
        val uid = activeUserId.value ?: return
        viewModelScope.launch {
            val existing = userProfileRepository.getUserProfile(uid)
            val updated = (existing ?: UserProfile(id = uid, name = name, email = null)).copy(
                name = name.trim(),
                monthlySalaryMinor = salaryMinor,
                updatedAt = Instant.now(),
                syncStatus = SyncState.PENDING
            )
            userProfileRepository.saveUserProfile(updated)
            dialogStates.update { it.copy(showEditProfile = false) }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            dialogStates.update { it.copy(isSyncing = true, syncMessage = null) }
            if (syncManager != null) {
                syncManager.syncNow()
            } else {
                val uid = activeUserId.value
                if (uid != null) {
                    runCatching {
                        userProfileRepository.syncUserProfile(uid)
                        categoryRepository.syncPendingCategories(uid)
                        expenseRepository.syncPendingExpenses(uid)
                        targetRepository.syncPendingTargets(uid)
                        debtCreditRepository.syncPendingDebtsAndCredits(uid)
                    }
                }
            }
            dialogStates.update { it.copy(isSyncing = false) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            dialogStates.update { it.copy(showLogout = false) }
            syncManager?.cancelAllSync()
            authRepository.signOut()
        }
    }

    fun showEditProfile(show: Boolean) {
        dialogStates.update { it.copy(showEditProfile = show) }
    }

    fun showThemeDialog(show: Boolean) {
        dialogStates.update { it.copy(showTheme = show) }
    }

    fun showLanguageDialog(show: Boolean) {
        dialogStates.update { it.copy(showLanguage = show) }
    }

    fun showCategoriesDialog(show: Boolean) {
        dialogStates.update { it.copy(showCategories = show) }
    }

    fun showLogoutConfirm(show: Boolean) {
        dialogStates.update { it.copy(showLogout = show) }
    }

    fun showDeleteAccountConfirm(show: Boolean) {
        dialogStates.update { it.copy(showDeleteAccount = show) }
    }

    fun showReauthDialog(show: Boolean) {
        dialogStates.update { it.copy(showReauthDialog = show, reauthError = null) }
    }

    fun deleteAccount(password: String? = null) {
        val uid = activeUserId.value ?: return
        viewModelScope.launch {
            dialogStates.update {
                it.copy(
                    isDeletingAccount = true,
                    showDeleteAccount = false,
                    reauthError = null
                )
            }
            val result = deleteAccountUseCase(userId = uid, password = password)
            result.onSuccess {
                dialogStates.update {
                    it.copy(
                        isDeletingAccount = false,
                        showReauthDialog = false,
                        reauthError = null
                    )
                }
            }.onFailure { error ->
                if (error is RecentLoginRequiredException) {
                    dialogStates.update {
                        it.copy(
                            isDeletingAccount = false,
                            showReauthDialog = true,
                            reauthError = error.localizedMessage
                        )
                    }
                } else {
                    dialogStates.update {
                        it.copy(
                            isDeletingAccount = false,
                            userMessage = error.localizedMessage ?: "Failed to delete account."
                        )
                    }
                }
            }
        }
    }

    fun clearUserMessage() {
        dialogStates.update { it.copy(userMessage = null) }
    }
}

