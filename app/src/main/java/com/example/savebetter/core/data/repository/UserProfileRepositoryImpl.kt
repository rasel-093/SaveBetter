package com.example.savebetter.core.data.repository

import com.example.savebetter.core.data.local.dao.UserDao
import com.example.savebetter.core.data.mapper.toDomain
import com.example.savebetter.core.data.mapper.toEntity
import com.example.savebetter.core.data.remote.UserProfileRemoteDataSource
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [UserProfileRepository].
 *
 * Local Room database serves as immediate source of truth.
 */
@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val remoteDataSource: UserProfileRemoteDataSource
) : UserProfileRepository {

    override fun observeUserProfile(userId: String): Flow<UserProfile?> =
        userDao.observeUser(userId).map { it?.toDomain() }

    override suspend fun getUserProfile(userId: String): UserProfile? =
        userDao.getUser(userId)?.toDomain()

    override suspend fun saveUserProfile(profile: UserProfile) {
        val pending = profile.copy(syncStatus = SyncState.PENDING, updatedAt = Instant.now())
        userDao.upsertUser(pending.toEntity())

        remoteDataSource.saveUserProfile(pending).onSuccess {
            userDao.updateSyncStatus(profile.id, SyncState.SYNCED, Instant.now())
        }.onFailure {
            userDao.updateSyncStatus(profile.id, SyncState.ERROR, Instant.now())
        }
    }

    override suspend fun updateOnboardingCompleted(userId: String, completed: Boolean) {
        val now = Instant.now()
        userDao.updateOnboardingCompleted(userId, completed, now, SyncState.PENDING)
        getUserProfile(userId)?.let { profile ->
            remoteDataSource.saveUserProfile(profile).onSuccess {
                userDao.updateSyncStatus(userId, SyncState.SYNCED, Instant.now())
            }
        }
    }

    override suspend fun syncUserProfile(userId: String): Result<Unit> = runCatching {
        remoteDataSource.fetchUserProfile(userId).getOrNull()?.let { remoteProfile ->
            userDao.upsertUser(remoteProfile.toEntity())
        }
    }

    override suspend fun deleteUserData(userId: String): Result<Unit> {
        return remoteDataSource.deleteUserData(userId)
    }
}

