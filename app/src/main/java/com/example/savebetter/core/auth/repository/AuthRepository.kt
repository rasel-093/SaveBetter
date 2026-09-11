package com.example.savebetter.core.auth.repository

import com.example.savebetter.core.auth.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for authentication and session management.
 *
 * Defines all authentication operations required by the application.
 * Implementations (e.g. Firebase, Django REST) live in the data layer.
 */
interface AuthRepository {

    /**
     * Emits the currently authenticated [AuthUser], or null if unauthenticated.
     * Re-emits whenever authentication state changes (login, logout, token refresh).
     */
    fun observeAuthState(): Flow<AuthUser?>

    /**
     * Authenticates an existing user using email and password.
     */
    suspend fun signIn(
        email: String,
        password: String
    ): Result<AuthUser>

    /**
     * Registers a new user using email and password.
     */
    suspend fun signUp(
        email: String,
        password: String
    ): Result<AuthUser>

    /**
     * Authenticates using a Google OAuth ID Token.
     */
    suspend fun signInWithGoogle(
        idToken: String
    ): Result<AuthUser>

    /**
     * Sends a password reset email to the given address.
     */
    suspend fun sendPasswordResetEmail(
        email: String
    ): Result<Unit>

    /**
     * Signs out the currently authenticated user.
     */
    suspend fun signOut()

    /**
     * Permanently deletes the current user's account.
     */
    suspend fun deleteAccount(): Result<Unit>
}
