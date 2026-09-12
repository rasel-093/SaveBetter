package com.example.savebetter.feature.settings

import android.content.Context
import app.cash.turbine.test
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.data.local.LanguagePreferences
import com.example.savebetter.core.data.local.SettingsPreferences
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.ThemeMode
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.DebtCreditRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import com.example.savebetter.core.domain.repository.UserProfileRepository
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.notification.ReconciliationReminderScheduler
import com.example.savebetter.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: Context = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk()
    private val userProfileRepository: UserProfileRepository = mockk()
    private val settingsPreferences: SettingsPreferences = mockk()
    private val languagePreferences: LanguagePreferences = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val expenseRepository: ExpenseRepository = mockk()
    private val targetRepository: TargetRepository = mockk()
    private val debtCreditRepository: DebtCreditRepository = mockk()

    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    private val vibrationFlow = MutableStateFlow(true)
    private val notificationsFlow = MutableStateFlow(true)
    private val weeklyWarningFlow = MutableStateFlow(true)
    private val monthlyWarningFlow = MutableStateFlow(true)
    private val reconciliationFlow = MutableStateFlow(true)
    private val languageFlow = MutableStateFlow(AppLanguage.ENGLISH)

    private val testUser = AuthUser(id = "user_settings_1", email = "test@example.com")
    private val testProfile = UserProfile(
        id = "user_settings_1",
        name = "Tanvir Ahmed",
        email = "test@example.com",
        monthlySalaryMinor = 6500000L,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
        syncStatus = SyncState.SYNCED
    )
    private val testCategory = Category(
        id = "cat_1",
        userId = "user_settings_1",
        nameKey = "category_grocery",
        icon = "grocery",
        colorToken = "cat1",
        isDefault = true
    )

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        mockkObject(ReconciliationReminderScheduler)
        every { ReconciliationReminderScheduler.scheduleMonthEndReminder(any()) } returns Unit
        every { ReconciliationReminderScheduler.cancelReminder(any()) } returns Unit

        every { authRepository.observeAuthState() } returns flowOf(testUser)
        every { userProfileRepository.observeUserProfile("user_settings_1") } returns flowOf(testProfile)
        coEvery { userProfileRepository.getUserProfile("user_settings_1") } returns testProfile
        coEvery { userProfileRepository.saveUserProfile(any()) } returns Unit

        every { categoryRepository.observeCategories("user_settings_1") } returns flowOf(listOf(testCategory))

        every { settingsPreferences.themeMode } returns themeModeFlow
        every { settingsPreferences.vibrationEnabled } returns vibrationFlow
        every { settingsPreferences.notificationsEnabled } returns notificationsFlow
        every { settingsPreferences.notifyWeeklyWarning } returns weeklyWarningFlow
        every { settingsPreferences.notifyMonthlyWarning } returns monthlyWarningFlow
        every { settingsPreferences.notifyReconciliation } returns reconciliationFlow
        every { languagePreferences.language } returns languageFlow

        coEvery { settingsPreferences.setThemeMode(any()) } returns Unit
        coEvery { settingsPreferences.setVibrationEnabled(any()) } returns Unit
        coEvery { settingsPreferences.setNotificationsEnabled(any()) } returns Unit
        coEvery { settingsPreferences.setNotifyWeeklyWarning(any()) } returns Unit
        coEvery { settingsPreferences.setNotifyMonthlyWarning(any()) } returns Unit
        coEvery { settingsPreferences.setNotifyReconciliation(any()) } returns Unit
        coEvery { languagePreferences.setLanguage(any()) } returns Unit

        coEvery { userProfileRepository.syncUserProfile(any()) } returns Result.success(Unit)
        coEvery { categoryRepository.syncPendingCategories(any()) } returns Result.success(Unit)
        coEvery { expenseRepository.syncPendingExpenses(any()) } returns Result.success(Unit)
        coEvery { targetRepository.syncPendingTargets(any()) } returns Result.success(Unit)
        coEvery { debtCreditRepository.syncPendingDebtsAndCredits(any()) } returns Result.success(Unit)
        coEvery { authRepository.signOut() } returns Unit

        viewModel = SettingsViewModel(
            context = context,
            authRepository = authRepository,
            userProfileRepository = userProfileRepository,
            settingsPreferences = settingsPreferences,
            languagePreferences = languagePreferences,
            categoryRepository = categoryRepository,
            expenseRepository = expenseRepository,
            targetRepository = targetRepository,
            debtCreditRepository = debtCreditRepository
        )
    }

    @After
    fun tearDown() {
        unmockkObject(ReconciliationReminderScheduler)
    }

    @Test
    fun `uiState loads user profile and settings preferences`() = runTest {
        viewModel.initUser("user_settings_1")
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }
            assertFalse(state.isLoading)
            assertEquals("Tanvir Ahmed", state.userName)
            assertEquals("test@example.com", state.userEmail)
            assertEquals(6500000L, state.monthlySalaryMinor)
            assertEquals(ThemeMode.SYSTEM, state.themeMode)
            assertTrue(state.vibrationEnabled)
            assertTrue(state.notificationsEnabled)
            assertEquals(AppLanguage.ENGLISH, state.currentLanguage)
            assertEquals(1, state.categories.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setThemeMode calls settingsPreferences and closes dialog`() = runTest {
        viewModel.initUser("user_settings_1")
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }
            assertFalse(state.showThemeDialog)

            viewModel.showThemeDialog(true)
            assertTrue(awaitItem().showThemeDialog)

            viewModel.setThemeMode(ThemeMode.DARK)
            assertFalse(awaitItem().showThemeDialog)
            coVerify { settingsPreferences.setThemeMode(ThemeMode.DARK) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setVibrationEnabled calls settingsPreferences`() = runTest {
        viewModel.setVibrationEnabled(false)
        testScheduler.advanceUntilIdle()

        coVerify { settingsPreferences.setVibrationEnabled(false) }
    }

    @Test
    fun `setNotificationsEnabled schedules reminder when true and cancels when false`() = runTest {
        viewModel.setNotificationsEnabled(true)
        testScheduler.advanceUntilIdle()

        coVerify { settingsPreferences.setNotificationsEnabled(true) }
        coVerify { ReconciliationReminderScheduler.scheduleMonthEndReminder(context) }

        viewModel.setNotificationsEnabled(false)
        testScheduler.advanceUntilIdle()

        coVerify { settingsPreferences.setNotificationsEnabled(false) }
        coVerify { ReconciliationReminderScheduler.cancelReminder(context) }
    }

    @Test
    fun `setNotifyReconciliation schedules reminder when true and cancels when false`() = runTest {
        viewModel.setNotifyReconciliation(true)
        testScheduler.advanceUntilIdle()

        coVerify { settingsPreferences.setNotifyReconciliation(true) }
        coVerify { ReconciliationReminderScheduler.scheduleMonthEndReminder(context) }

        viewModel.setNotifyReconciliation(false)
        testScheduler.advanceUntilIdle()

        coVerify { settingsPreferences.setNotifyReconciliation(false) }
        coVerify { ReconciliationReminderScheduler.cancelReminder(context) }
    }

    @Test
    fun `setLanguage calls languagePreferences and closes dialog`() = runTest {
        viewModel.initUser("user_settings_1")
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }
            assertFalse(state.showLanguageDialog)

            viewModel.showLanguageDialog(true)
            assertTrue(awaitItem().showLanguageDialog)

            viewModel.setLanguage(AppLanguage.BANGLA)
            assertFalse(awaitItem().showLanguageDialog)
            coVerify { languagePreferences.setLanguage(AppLanguage.BANGLA) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateProfile saves updated user profile to repository`() = runTest {
        viewModel.initUser("user_settings_1")
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }
            assertFalse(state.showEditProfileDialog)

            viewModel.showEditProfile(true)
            assertTrue(awaitItem().showEditProfileDialog)

            viewModel.updateProfile("Tanvir Hossain", 7000000L)
            assertFalse(awaitItem().showEditProfileDialog)

            coVerify {
                userProfileRepository.saveUserProfile(
                    match {
                        it.id == "user_settings_1" &&
                        it.name == "Tanvir Hossain" &&
                        it.monthlySalaryMinor == 7000000L &&
                        it.syncStatus == SyncState.PENDING
                    }
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `syncNow triggers sync across all repositories`() = runTest {
        viewModel.initUser("user_settings_1")

        viewModel.syncNow()
        testScheduler.advanceUntilIdle()

        coVerify { userProfileRepository.syncUserProfile("user_settings_1") }
        coVerify { categoryRepository.syncPendingCategories("user_settings_1") }
        coVerify { expenseRepository.syncPendingExpenses("user_settings_1") }
        coVerify { targetRepository.syncPendingTargets("user_settings_1") }
        coVerify { debtCreditRepository.syncPendingDebtsAndCredits("user_settings_1") }
    }

    @Test
    fun `logout calls authRepository signOut`() = runTest {
        viewModel.logout()
        testScheduler.advanceUntilIdle()

        coVerify { authRepository.signOut() }
    }

    @Test
    fun `dialog state toggles function correctly`() = runTest {
        viewModel.initUser("user_settings_1")
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }
            assertFalse(state.showEditProfileDialog)

            viewModel.showEditProfile(true)
            assertTrue(awaitItem().showEditProfileDialog)
            viewModel.showEditProfile(false)
            assertFalse(awaitItem().showEditProfileDialog)

            viewModel.showCategoriesDialog(true)
            assertTrue(awaitItem().showCategoriesDialog)
            viewModel.showCategoriesDialog(false)
            assertFalse(awaitItem().showCategoriesDialog)

            viewModel.showLogoutConfirm(true)
            assertTrue(awaitItem().showLogoutConfirmDialog)
            viewModel.showLogoutConfirm(false)
            assertFalse(awaitItem().showLogoutConfirmDialog)

            viewModel.showDeleteAccountConfirm(true)
            assertTrue(awaitItem().showDeleteAccountConfirmDialog)
            viewModel.showDeleteAccountConfirm(false)
            assertFalse(awaitItem().showDeleteAccountConfirmDialog)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
