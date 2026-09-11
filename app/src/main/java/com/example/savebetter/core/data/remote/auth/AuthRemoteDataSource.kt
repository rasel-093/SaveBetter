package com.example.savebetter.core.data.remote.auth

import com.example.savebetter.core.auth.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Remote data source abstraction for authentication.
 *
 * This contract enables swapping Firebase Authentication for Django REST API
 * (or any custom backend) in the future without altering repository interfaces,
 * use cases, or UI.
 */
interface AuthRemoteDataSource {

    /**
     * Observes the remote authentication state changes.
     */
    fun observeAuthState(): Flow<AuthUser?>

    /**
     * Signs in with email and password on the remote service.
     */
    suspend fun signIn(email: String, password: String): Result<AuthUser>

    /**
     * Registers a new account with email and password on the remote service.
     */
    suspend fun signUp(email: String, password: String): Result<AuthUser>

    /**
     * Signs in using a Google ID token.
     */
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>

    /**
     * Requests a password reset email from the remote service.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Signs out from the remote service.
     */
    suspend fun signOut()

    /**
     * Permanently deletes the remote account.
     */
    suspend fun deleteAccount(): Result<Unit>
}
