package com.example.savebetter.core.data.repository

import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.data.remote.auth.AuthRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AuthRepository].
 *
 * Coordinates between the [AuthRemoteDataSource] (currently Firebase,
 * later swappable with Django REST API) and local persistence when user
 * sessions change.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override fun observeAuthState(): Flow<AuthUser?> {
        return remoteDataSource.observeAuthState()
    }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return remoteDataSource.signIn(email, password)
    }

    override suspend fun signUp(email: String, password: String): Result<AuthUser> {
        return remoteDataSource.signUp(email, password)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        return remoteDataSource.signInWithGoogle(idToken)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return remoteDataSource.sendPasswordResetEmail(email)
    }

    override suspend fun signOut() {
        remoteDataSource.signOut()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return remoteDataSource.deleteAccount()
    }
}
