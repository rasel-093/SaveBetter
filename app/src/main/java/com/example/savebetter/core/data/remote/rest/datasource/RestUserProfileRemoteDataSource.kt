package com.example.savebetter.core.data.remote.rest.datasource

import com.example.savebetter.core.data.remote.UserProfileRemoteDataSource
import com.example.savebetter.core.data.remote.rest.api.SaveBetterRestApi
import com.example.savebetter.core.data.remote.rest.mapper.toDomain
import com.example.savebetter.core.data.remote.rest.mapper.toRestDto
import com.example.savebetter.core.domain.model.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RESTful backend implementation of [UserProfileRemoteDataSource].
 */
@Singleton
class RestUserProfileRemoteDataSource @Inject constructor(
    private val restApi: SaveBetterRestApi
) : UserProfileRemoteDataSource {

    override suspend fun fetchUserProfile(userId: String): Result<UserProfile?> {
        return restApi.getUserProfile(userId).map { it?.toDomain() }
    }

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return restApi.saveUserProfile(profile.id, profile.toRestDto())
    }

    override suspend fun deleteUserData(userId: String): Result<Unit> {
        return restApi.deleteUserData(userId)
    }
}
