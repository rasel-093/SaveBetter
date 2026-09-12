package com.example.savebetter.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebetter.core.domain.usecase.auth.SendPasswordResetUseCase
import com.example.savebetter.core.domain.usecase.auth.SignInUseCase
import com.example.savebetter.core.domain.usecase.auth.SignInWithGoogleUseCase
import com.example.savebetter.core.domain.usecase.auth.SignOutUseCase
import com.example.savebetter.core.domain.usecase.auth.SignUpUseCase
import com.example.savebetter.core.domain.repository.SyncRepository
import com.example.savebetter.core.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isSent: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel managing authentication forms (Login, Sign Up, Forgot Password)
 * and triggering auth actions.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val syncRepository: SyncRepository? = null,
    private val syncManager: SyncManager? = null
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow(SignUpUiState())
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordUiState())
    val forgotPasswordState: StateFlow<ForgotPasswordUiState> = _forgotPasswordState.asStateFlow()

    // ── Login Actions ────────────────────────────────────────────────────────

    fun onLoginEmailChange(email: String) {
        _loginState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onLoginPasswordChange(password: String) {
        _loginState.update { it.copy(password = password, errorMessage = null) }
    }

    fun signIn(onSuccess: () -> Unit = {}) {
        val currentState = _loginState.value
        if (currentState.email.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Please enter your email address.") }
            return
        }
        if (currentState.password.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Please enter your password.") }
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = signInUseCase(currentState.email, currentState.password)
            result.fold(
                onSuccess = { user ->
                    // Multi-Device: Pull remote records into local Room database so UI observes populated Room immediately
                    runCatching { syncRepository?.pullRemote(user.id) }
                    syncManager?.requestImmediateSync()
                    _loginState.update { it.copy(isLoading = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Sign in failed."
                        )
                    }
                }
            )
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = signInWithGoogleUseCase(idToken)
            result.fold(
                onSuccess = { user ->
                    // Multi-Device: Pull remote records into local Room database
                    runCatching { syncRepository?.pullRemote(user.id) }
                    syncManager?.requestImmediateSync()
                    _loginState.update { it.copy(isLoading = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Google sign in failed."
                        )
                    }
                }
            )
        }
    }

    // ── Sign Up Actions ──────────────────────────────────────────────────────

    fun onSignUpEmailChange(email: String) {
        _signUpState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onSignUpPasswordChange(password: String) {
        _signUpState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onSignUpConfirmPasswordChange(password: String) {
        _signUpState.update { it.copy(confirmPassword = password, errorMessage = null) }
    }

    fun signUp(onSuccess: () -> Unit = {}) {
        val currentState = _signUpState.value
        if (currentState.password != currentState.confirmPassword) {
            _signUpState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _signUpState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = signUpUseCase(
                email = currentState.email,
                password = currentState.password,
                confirmPassword = currentState.confirmPassword
            )
            result.fold(
                onSuccess = {
                    syncManager?.requestImmediateSync()
                    _signUpState.update { it.copy(isLoading = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _signUpState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Sign up failed."
                        )
                    }
                }
            )
        }
    }

    // ── Forgot Password Actions ──────────────────────────────────────────────

    fun onForgotPasswordEmailChange(email: String) {
        _forgotPasswordState.update { it.copy(email = email, errorMessage = null) }
    }

    fun sendPasswordReset() {
        val currentState = _forgotPasswordState.value
        viewModelScope.launch {
            _forgotPasswordState.update { it.copy(isLoading = true, errorMessage = null, isSent = false) }
            val result = sendPasswordResetUseCase(currentState.email)
            result.fold(
                onSuccess = {
                    _forgotPasswordState.update { it.copy(isLoading = false, isSent = true) }
                },
                onFailure = { error ->
                    _forgotPasswordState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to send reset link."
                        )
                    }
                }
            )
        }
    }

    fun resetForgotPasswordStatus() {
        _forgotPasswordState.update { ForgotPasswordUiState() }
    }

    // ── Session Actions ──────────────────────────────────────────────────────

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }

    fun clearLoginError() {
        _loginState.update { it.copy(errorMessage = null) }
    }

    fun clearSignUpError() {
        _signUpState.update { it.copy(errorMessage = null) }
    }
}
