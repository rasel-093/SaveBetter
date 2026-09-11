package com.example.savebetter.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.savebetter.core.data.local.entity.UserEntity
import com.example.savebetter.core.domain.model.SyncState
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Data access object for the User entity.
 */
@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUser(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUser(userId: String): UserEntity?

    @Upsert
    suspend fun upsertUser(user: UserEntity)

    @Query("UPDATE users SET onboardingCompleted = :completed, updatedAt = :updatedAt, syncStatus = :syncStatus WHERE id = :userId")
    suspend fun updateOnboardingCompleted(userId: String, completed: Boolean, updatedAt: Instant, syncStatus: SyncState)

    @Query("UPDATE users SET syncStatus = :syncStatus, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateSyncStatus(userId: String, syncStatus: SyncState, updatedAt: Instant)
}
