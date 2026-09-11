package com.example.savebetter.core.domain.usecase.auth

import com.example.savebetter.core.auth.model.AuthException
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case to authenticate with a Google OAuth ID Token.
 */
class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<AuthUser> {
        if (idToken.isBlank()) {
            return Result.failure(AuthException("Google ID Token is missing."))
        }
        return authRepository.signInWithGoogle(idToken)
    }
}
