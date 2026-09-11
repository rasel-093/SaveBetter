package com.example.savebetter.core.domain.usecase.auth

import com.example.savebetter.core.auth.model.AuthException
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case to sign in with email and password.
 */
class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthUser> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            return Result.failure(AuthException("Email address cannot be empty."))
        }
        if (password.isBlank()) {
            return Result.failure(AuthException("Password cannot be empty."))
        }
        return authRepository.signIn(trimmedEmail, password)
    }
}
