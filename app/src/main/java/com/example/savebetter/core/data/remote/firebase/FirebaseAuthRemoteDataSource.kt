package com.example.savebetter.core.data.remote.firebase

import com.example.savebetter.core.auth.model.AuthException
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.auth.model.RecentLoginRequiredException
import com.example.savebetter.core.data.remote.auth.AuthRemoteDataSource
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Authentication implementation of [AuthRemoteDataSource].
 *
 * Encapsulates all Firebase SDK classes and maps Firebase exceptions to
 * domain [AuthException]s with user-friendly messages.
 */
@Singleton
class FirebaseAuthRemoteDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRemoteDataSource {

    override fun observeAuthState(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser?.toAuthUser()
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user?.toAuthUser()
                ?: throw AuthException("Authentication returned an empty user.")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun signUp(email: String, password: String): Result<AuthUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user?.toAuthUser()
                ?: throw AuthException("Account creation returned an empty user.")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user?.toAuthUser()
                ?: throw AuthException("Google Sign-In returned an empty user.")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun reauthenticate(password: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: throw AuthException("No user is currently signed in.")
            val email = currentUser.email
                ?: throw AuthException("Current user has no associated email address.")
            val credential = EmailAuthProvider.getCredential(email, password)
            currentUser.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: throw AuthException("No user is currently signed in.")
            currentUser.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    private fun FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            id = uid,
            email = email,
            displayName = displayName
        )
    }

    private fun mapFirebaseException(e: Exception): AuthException {
        return when (e) {
            is FirebaseAuthRecentLoginRequiredException ->
                RecentLoginRequiredException(
                    "This sensitive operation requires recent authentication. Please re-authenticate before retrying.",
                    e
                )
            is FirebaseAuthInvalidUserException ->
                AuthException("No account found with this email.", e)
            is FirebaseAuthInvalidCredentialsException ->
                AuthException("Invalid credentials or incorrect password.", e)
            is FirebaseAuthUserCollisionException ->
                AuthException("An account with this email already exists.", e)
            is FirebaseAuthWeakPasswordException ->
                AuthException("Password is too weak. Please use at least 6 characters.", e)
            is FirebaseNetworkException ->
                AuthException("Network error. Please check your internet connection.", e)
            is FirebaseTooManyRequestsException ->
                AuthException("Too many unsuccessful attempts. Please try again later.", e)
            is AuthException -> e
            else -> AuthException(e.localizedMessage ?: "Authentication failed. Please try again.", e)
        }
    }
}

