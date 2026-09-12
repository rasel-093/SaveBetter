package com.example.savebetter.core.auth.model

/**
 * Unique identifier representing an authenticated user.
 */
typealias UserId = String

/**
 * Firebase-independent domain model for an authenticated user.
 *
 * This model strictly isolates the domain and UI layers from
 * FirebaseUser or any backend-specific user objects.
 */
data class AuthUser(
    val id: UserId,
    val email: String?,
    val displayName: String? = null
)

/**
 * Domain-level exception representing authentication failures.
 *
 * Used to translate backend-specific (e.g. Firebase SDK) exceptions into
 * user-friendly, localized error messages without leaking SDK details.
 */
open class AuthException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when a sensitive operation (such as account deletion) requires
 * recent authentication before it can proceed.
 */
class RecentLoginRequiredException(
    message: String = "This sensitive operation requires recent authentication. Please verify your credentials.",
    cause: Throwable? = null
) : AuthException(message, cause)

