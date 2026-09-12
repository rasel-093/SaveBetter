package com.example.savebetter.core.data.remote.rest.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * REST API DTOs for authentication and authorization.
 *
 * Applicable to any backend stack (Django REST Framework, FastAPI, Spring Boot, Express, Go).
 */

@Serializable
data class RestLoginRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class RestRegisterRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("display_name") val displayName: String? = null
)

@Serializable
data class RestPasswordResetRequest(
    @SerialName("email") val email: String
)

@Serializable
data class RestRefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class RestGoogleSignInRequest(
    @SerialName("id_token") val idToken: String
)

@Serializable
data class RestAuthResponse(
    @SerialName("user_id") val userId: String,
    @SerialName("email") val email: String?,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("token_type") val tokenType: String = "Bearer"
)
