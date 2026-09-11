package com.example.savebetter.feature.auth

import app.cash.turbine.test
import com.example.savebetter.core.auth.model.AuthException
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.domain.usecase.auth.SendPasswordResetUseCase
import com.example.savebetter.core.domain.usecase.auth.SignInUseCase
import com.example.savebetter.core.domain.usecase.auth.SignInWithGoogleUseCase
import com.example.savebetter.core.domain.usecase.auth.SignOutUseCase
import com.example.savebetter.core.domain.usecase.auth.SignUpUseCase
import com.example.savebetter.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val signInUseCase: SignInUseCase = mockk()
    private val signUpUseCase: SignUpUseCase = mockk()
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase = mockk()
    private val sendPasswordResetUseCase: SendPasswordResetUseCase = mockk()
    private val signOutUseCase: SignOutUseCase = mockk(relaxed = true)

    private lateinit var viewModel: AuthViewModel

    private val testUser = AuthUser(
        id = "user_123",
        email = "test@example.com",
        displayName = "Test User"
    )

    @Before
    fun setUp() {
        viewModel = AuthViewModel(
            signInUseCase = signInUseCase,
            signUpUseCase = signUpUseCase,
            signInWithGoogleUseCase = signInWithGoogleUseCase,
            sendPasswordResetUseCase = sendPasswordResetUseCase,
            signOutUseCase = signOutUseCase
        )
    }

    @Test
    fun `signIn with empty email sets error message without invoking use case`() = runTest {
        viewModel.onLoginEmailChange("")
        viewModel.onLoginPasswordChange("password123")
        viewModel.signIn()

        val state = viewModel.loginState.value
        assertEquals("Please enter your email address.", state.errorMessage)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { signInUseCase(any(), any()) }
    }

    @Test
    fun `signIn with empty password sets error message without invoking use case`() = runTest {
        viewModel.onLoginEmailChange("test@example.com")
        viewModel.onLoginPasswordChange("")
        viewModel.signIn()

        val state = viewModel.loginState.value
        assertEquals("Please enter your password.", state.errorMessage)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { signInUseCase(any(), any()) }
    }

    @Test
    fun `signIn success triggers success callback and resets loading`() = runTest {
        coEvery { signInUseCase("test@example.com", "password123") } returns Result.success(testUser)

        var callbackCalled = false
        viewModel.onLoginEmailChange("test@example.com")
        viewModel.onLoginPasswordChange("password123")
        viewModel.signIn(onSuccess = { callbackCalled = true })

        val state = viewModel.loginState.value
        assertTrue(callbackCalled)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `signIn failure sets error message`() = runTest {
        coEvery { signInUseCase("test@example.com", "wrong") } returns Result.failure(
            AuthException("Invalid credentials or incorrect password.")
        )

        viewModel.onLoginEmailChange("test@example.com")
        viewModel.onLoginPasswordChange("wrong")
        viewModel.signIn()

        val state = viewModel.loginState.value
        assertEquals("Invalid credentials or incorrect password.", state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `signUp with mismatched passwords sets error without invoking usecase`() = runTest {
        viewModel.onSignUpEmailChange("new@example.com")
        viewModel.onSignUpPasswordChange("password123")
        viewModel.onSignUpConfirmPasswordChange("different123")
        viewModel.signUp()

        val state = viewModel.signUpState.value
        assertEquals("Passwords do not match.", state.errorMessage)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { signUpUseCase(any(), any(), any()) }
    }

    @Test
    fun `signUp success triggers callback and clears error`() = runTest {
        coEvery {
            signUpUseCase("new@example.com", "password123", "password123")
        } returns Result.success(testUser)

        var callbackCalled = false
        viewModel.onSignUpEmailChange("new@example.com")
        viewModel.onSignUpPasswordChange("password123")
        viewModel.onSignUpConfirmPasswordChange("password123")
        viewModel.signUp(onSuccess = { callbackCalled = true })

        val state = viewModel.signUpState.value
        assertTrue(callbackCalled)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `sendPasswordReset success sets isSent true`() = runTest {
        coEvery { sendPasswordResetUseCase("reset@example.com") } returns Result.success(Unit)

        viewModel.onForgotPasswordEmailChange("reset@example.com")
        viewModel.sendPasswordReset()

        val state = viewModel.forgotPasswordState.value
        assertTrue(state.isSent)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `signOut delegates to signOutUseCase`() = runTest {
        viewModel.signOut()

        coVerify(exactly = 1) { signOutUseCase() }
    }
}
