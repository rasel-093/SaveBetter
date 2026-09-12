package com.example.savebetter.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.domain.usecase.auth.SendPasswordResetUseCase
import com.example.savebetter.core.domain.usecase.auth.SignInUseCase
import com.example.savebetter.core.domain.usecase.auth.SignInWithGoogleUseCase
import com.example.savebetter.core.domain.usecase.auth.SignOutUseCase
import com.example.savebetter.core.domain.usecase.auth.SignUpUseCase
import com.example.savebetter.feature.auth.AuthViewModel
import com.example.savebetter.feature.auth.ui.SignUpScreen
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        viewModel = AuthViewModel(
            signInUseCase = SignInUseCase(authRepository),
            signUpUseCase = SignUpUseCase(authRepository),
            signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository),
            sendPasswordResetUseCase = SendPasswordResetUseCase(authRepository),
            signOutUseCase = SignOutUseCase(authRepository)
        )
    }

    @Test
    fun signUpScreen_rendersAllFields() {
        composeTestRule.setContent {
            SaveBetterTheme {
                SignUpScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = {}
                )
            }
        }

        val signupTitle = context.getString(R.string.signup_title)
        val emailLabel = context.getString(R.string.email_label)
        val passwordLabel = context.getString(R.string.password_label)
        val confirmPasswordLabel = context.getString(R.string.confirm_password_label)
        val signInLink = context.getString(R.string.sign_in_link)

        composeTestRule.onAllNodes(androidx.compose.ui.test.hasText(signupTitle))[0].assertIsDisplayed()
        composeTestRule.onNodeWithText(emailLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(passwordLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(confirmPasswordLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(signInLink).assertIsDisplayed()
    }

    @Test
    fun signUpScreen_clickingSignInLink_invokesCallback() {
        var navigatedToLogin = false

        composeTestRule.setContent {
            SaveBetterTheme {
                SignUpScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { navigatedToLogin = true }
                )
            }
        }

        val signInLink = context.getString(R.string.sign_in_link)
        composeTestRule.onNodeWithText(signInLink).performClick()

        assertTrue(navigatedToLogin)
    }
}
