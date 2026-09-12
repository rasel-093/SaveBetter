package com.example.savebetter.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import com.example.savebetter.feature.auth.ui.LoginScreen
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

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
    fun loginScreen_rendersAllEssentialElements() {
        composeTestRule.setContent {
            SaveBetterTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToSignUp = {},
                    onNavigateToForgotPassword = {}
                )
            }
        }

        val loginTitle = context.getString(R.string.login_title)
        val emailLabel = context.getString(R.string.email_label)
        val passwordLabel = context.getString(R.string.password_label)
        val loginButton = context.getString(R.string.login_button)
        val forgotPassword = context.getString(R.string.forgot_password_link)
        val signUpLink = context.getString(R.string.sign_up_link)

        composeTestRule.onNodeWithText(loginTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(emailLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(passwordLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(loginButton).assertIsDisplayed()
        composeTestRule.onNodeWithText(forgotPassword).assertIsDisplayed()
        composeTestRule.onNodeWithText(signUpLink).assertIsDisplayed()
    }

    @Test
    fun loginScreen_enteringEmailAndPassword_updatesState() {
        composeTestRule.setContent {
            SaveBetterTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToSignUp = {},
                    onNavigateToForgotPassword = {}
                )
            }
        }

        val emailPlaceholder = context.getString(R.string.email_placeholder)
        val passwordPlaceholder = context.getString(R.string.password_placeholder)

        composeTestRule.onNodeWithText(emailPlaceholder).performTextInput("test@savebetter.app")
        composeTestRule.onNodeWithText(passwordPlaceholder).performTextInput("Secret123!")

        assertTrue(viewModel.loginState.value.email == "test@savebetter.app")
        assertTrue(viewModel.loginState.value.password == "Secret123!")
    }

    @Test
    fun loginScreen_clickingSignUp_invokesCallback() {
        var navigatedToSignUp = false

        composeTestRule.setContent {
            SaveBetterTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToSignUp = { navigatedToSignUp = true },
                    onNavigateToForgotPassword = {}
                )
            }
        }

        val signUpLink = context.getString(R.string.sign_up_link)
        composeTestRule.onNodeWithText(signUpLink).performClick()

        assertTrue(navigatedToSignUp)
    }

    @Test
    fun loginScreen_clickingForgotPassword_invokesCallback() {
        var navigatedToForgotPassword = false

        composeTestRule.setContent {
            SaveBetterTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToSignUp = {},
                    onNavigateToForgotPassword = { navigatedToForgotPassword = true }
                )
            }
        }

        val forgotPasswordLink = context.getString(R.string.forgot_password_link)
        composeTestRule.onNodeWithText(forgotPasswordLink).performClick()

        assertTrue(navigatedToForgotPassword)
    }
}
