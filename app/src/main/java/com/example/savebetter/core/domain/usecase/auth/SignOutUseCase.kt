package com.example.savebetter.core.domain.usecase.auth

import com.example.savebetter.core.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case to sign out the current user and terminate the active session.
 */
class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.signOut()
    }
}
