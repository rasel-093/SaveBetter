package com.example.savebetter.core.data.remote.rest.datasource

import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.data.remote.auth.AuthRemoteDataSource
import com.example.savebetter.core.data.remote.rest.api.SaveBetterRestApi
import com.example.savebetter.core.data.remote.rest.dto.RestGoogleSignInRequest
import com.example.savebetter.core.data.remote.rest.dto.RestLoginRequest
import com.example.savebetter.core.data.remote.rest.dto.RestPasswordResetRequest
import com.example.savebetter.core.data.remote.rest.dto.RestRegisterRequest
import com.example.savebetter.core.data.remote.rest.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RESTful backend implementation of [AuthRemoteDataSource].
 *
 * Applicable to any custom REST API backend (Django REST Framework, FastAPI, Spring Boot, etc.)
 * managing JWT authentication tokens and session lifecycles.
 */
@Singleton
class RestAuthRemoteDataSource @Inject constructor(
    private val restApi: SaveBetterRestApi
) : AuthRemoteDataSource {

    private val _currentAuthUser = MutableStateFlow<AuthUser?>(null)

    override fun observeAuthState(): Flow<AuthUser?> = _currentAuthUser.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return restApi.login(RestLoginRequest(email = email, password = password))
            .map { response ->
                val authUser = response.toDomain()
                _currentAuthUser.value = authUser
                authUser
            }
    }

    override suspend fun signUp(email: String, password: String): Result<AuthUser> {
        return restApi.register(RestRegisterRequest(email = email, password = password))
            .map { response ->
                val authUser = response.toDomain()
                _currentAuthUser.value = authUser
                authUser
            }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        return restApi.signInWithGoogle(RestGoogleSignInRequest(idToken = idToken))
            .map { response ->
                val authUser = response.toDomain()
                _currentAuthUser.value = authUser
                authUser
            }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return restApi.sendPasswordResetEmail(RestPasswordResetRequest(email = email))
    }

    override suspend fun signOut() {
        runCatching { restApi.logout() }
        _currentAuthUser.value = null
    }

    override suspend fun reauthenticate(password: String): Result<Unit> {
        val currentUser = _currentAuthUser.value ?: return Result.failure(IllegalStateException("No active user"))
        val email = currentUser.email ?: return Result.failure(IllegalStateException("User has no email"))
        return restApi.login(RestLoginRequest(email = email, password = password)).map { Unit }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return restApi.deleteAccount().onSuccess {
            _currentAuthUser.value = null
        }
    }
}
