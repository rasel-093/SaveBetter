package com.example.savebetter.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.savebetter.R
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.data.local.LanguagePreferences
import com.example.savebetter.core.data.local.SaveBetterDatabase
import com.example.savebetter.core.data.local.SettingsPreferences
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.usecase.auth.DeleteAccountUseCase
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.feature.settings.SettingsViewModel
import com.example.savebetter.feature.settings.ui.SettingsScreen
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SettingsAndLogoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var database: SaveBetterDatabase
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var settingsPreferences: SettingsPreferences
    private lateinit var languagePreferences: LanguagePreferences
    private lateinit var viewModel: SettingsViewModel

    private lateinit var settingsFile: File
    private lateinit var langFile: File

    @Before
    fun setUp() {
        val testUser = AuthUser("user_123", "nafis@example.com", "Nafis Ahmed")
        authRepository = FakeAuthRepository(testUser)
        userProfileRepository = FakeUserProfileRepository(
            UserProfile(id = "user_123", name = "Nafis Ahmed", email = "nafis@example.com")
        )

        settingsFile = File(context.filesDir, "test_settings_${UUID.randomUUID()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create { settingsFile }
        settingsPreferences = SettingsPreferences(dataStore)

        langFile = File(context.filesDir, "test_lang_${UUID.randomUUID()}.preferences_pb")
        val langDataStore = PreferenceDataStoreFactory.create { langFile }
        languagePreferences = LanguagePreferences(langDataStore)

        database = Room.inMemoryDatabaseBuilder(context, SaveBetterDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val deleteAccountUseCase = DeleteAccountUseCase(
            authRepository = authRepository,
            userProfileRepository = userProfileRepository,
            settingsPreferences = settingsPreferences,
            database = database,
            context = context
        )

        viewModel = SettingsViewModel(
            context = context,
            authRepository = authRepository,
            userProfileRepository = userProfileRepository,
            settingsPreferences = settingsPreferences,
            languagePreferences = languagePreferences,
            categoryRepository = FakeCategoryRepository(),
            expenseRepository = FakeExpenseRepository(),
            targetRepository = FakeTargetRepository(),
            debtCreditRepository = FakeDebtCreditRepository(),
            deleteAccountUseCase = deleteAccountUseCase
        )
        viewModel.initUser("user_123")
    }

    @After
    fun tearDown() {
        database.close()
        settingsFile.delete()
        langFile.delete()
    }

    @Test
    fun settingsScreen_rendersProfileAndSections() {
        composeTestRule.setContent {
            SaveBetterTheme {
                SettingsScreen(
                    userId = "user_123",
                    selectedLanguage = AppLanguage.ENGLISH,
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()

        val displaySection = context.getString(R.string.settings_section_display)
        val notificationsSection = context.getString(R.string.settings_section_notifications)
        val dataSyncSection = context.getString(R.string.settings_section_data_sync)
        val logoutText = context.getString(R.string.settings_logout)

        composeTestRule.onNodeWithText("Nafis Ahmed").assertIsDisplayed()
        composeTestRule.onNodeWithText(displaySection, ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(notificationsSection, ignoreCase = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(dataSyncSection, ignoreCase = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(logoutText).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun settingsScreen_clickingLogout_showsConfirmationAndCallsSignOut() {
        composeTestRule.setContent {
            SaveBetterTheme {
                SettingsScreen(
                    userId = "user_123",
                    selectedLanguage = AppLanguage.ENGLISH,
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()

        val logoutText = context.getString(R.string.settings_logout)
        val confirmDesc = context.getString(R.string.settings_logout_confirm_desc)

        // Scroll to and click Log Out row
        composeTestRule.onAllNodes(hasText(logoutText))[0].performScrollTo().performClick()

        // Verify confirm dialog is displayed via unique description
        composeTestRule.onNodeWithText(confirmDesc).assertIsDisplayed()

        // Confirm logout in dialog (the confirm button is the last node with logoutText)
        val logoutButtons = composeTestRule.onAllNodes(hasText(logoutText))
        logoutButtons[logoutButtons.fetchSemanticsNodes().lastIndex].performClick()

        // Verify authRepository.signOut() was invoked
        assertTrue(authRepository.signOutCalled)
    }
}
