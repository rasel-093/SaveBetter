package com.example.savebetter.core.domain.repository

import com.example.savebetter.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Backend-agnostic repository interface for user profile data.
 */
interface UserProfileRepository {
    fun observeUserProfile(userId: String): Flow<UserProfile?>
    suspend fun getUserProfile(userId: String): UserProfile?
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun updateOnboardingCompleted(userId: String, completed: Boolean)
    suspend fun syncUserProfile(userId: String): Result<Unit>
}
