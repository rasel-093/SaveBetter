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
import com.example.savebetter.navigation.AuthNavHost
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthNavigationTest {

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
    fun authNavHost_navigatesFromLoginToSignUpAndBack() {
        composeTestRule.setContent {
            SaveBetterTheme {
                AuthNavHost(viewModel = viewModel)
            }
        }

        val loginTitle = context.getString(R.string.login_title)
        val signUpLink = context.getString(R.string.sign_up_link)
        val signupTitle = context.getString(R.string.signup_title)
        val signInLink = context.getString(R.string.sign_in_link)

        // Verify initially on Login
        composeTestRule.onNodeWithText(loginTitle).assertIsDisplayed()

        // Navigate to Sign Up
        composeTestRule.onNodeWithText(signUpLink).performClick()

        // Verify on Sign Up screen
        composeTestRule.onAllNodes(androidx.compose.ui.test.hasText(signupTitle))[0].assertIsDisplayed()

        // Navigate back to Login
        composeTestRule.onNodeWithText(signInLink).performClick()

        // Verify back on Login screen
        composeTestRule.onNodeWithText(loginTitle).assertIsDisplayed()
    }

    @Test
    fun authNavHost_navigatesFromLoginToForgotPasswordAndBack() {
        composeTestRule.setContent {
            SaveBetterTheme {
                AuthNavHost(viewModel = viewModel)
            }
        }

        val loginTitle = context.getString(R.string.login_title)
        val forgotPasswordLink = context.getString(R.string.forgot_password_link)
        val forgotPasswordTitle = context.getString(R.string.forgot_password_title)
        val backToLogin = context.getString(R.string.back_to_login)

        // Click forgot password
        composeTestRule.onNodeWithText(forgotPasswordLink).performClick()

        // Verify on Forgot Password screen
        composeTestRule.onNodeWithText(forgotPasswordTitle).assertIsDisplayed()

        // Click back to login
        composeTestRule.onNodeWithText(backToLogin).performClick()

        // Verify back on Login screen
        composeTestRule.onNodeWithText(loginTitle).assertIsDisplayed()
    }
}
