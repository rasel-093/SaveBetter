package com.example.savebetter.core.data.remote

import com.example.savebetter.core.domain.model.UserProfile

/**
 * Backend-agnostic remote data source interface for user profile data.
 *
 * Current implementation: [com.example.savebetter.core.data.remote.firebase.FirebaseUserProfileRemoteDataSource]
 * Future implementation: Django REST API / PostgreSQL data source.
 */
interface UserProfileRemoteDataSource {
    suspend fun fetchUserProfile(userId: String): Result<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit>
    suspend fun deleteUserData(userId: String): Result<Unit>
}

