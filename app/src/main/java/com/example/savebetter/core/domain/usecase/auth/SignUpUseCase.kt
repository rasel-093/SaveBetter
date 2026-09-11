package com.example.savebetter.core.domain.usecase.auth

import com.example.savebetter.core.auth.model.AuthException
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case to register a new user account with email and password.
 */
class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String? = null
    ): Result<AuthUser> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            return Result.failure(AuthException("Email address cannot be empty."))
        }
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!trimmedEmail.matches(emailRegex)) {
            return Result.failure(AuthException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(AuthException("Password must be at least 6 characters."))
        }
        if (confirmPassword != null && password != confirmPassword) {
            return Result.failure(AuthException("Passwords do not match."))
        }
        return authRepository.signUp(trimmedEmail, password)
    }
}
