package com.example.savebetter.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.savebetter.core.data.local.entity.MonthlyTargetEntity
import com.example.savebetter.core.domain.model.SyncState
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for the MonthlyTarget entity.
 */
@Dao
interface MonthlyTargetDao {

    @Query("SELECT * FROM monthly_targets WHERE userId = :userId ORDER BY year DESC, month DESC")
    fun observeMonthlyTargets(userId: String): Flow<List<MonthlyTargetEntity>>

    @Query("SELECT * FROM monthly_targets WHERE userId = :userId AND year = :year AND month = :month LIMIT 1")
    suspend fun getMonthlyTarget(userId: String, year: Int, month: Int): MonthlyTargetEntity?

    @Query("SELECT * FROM monthly_targets WHERE id = :id LIMIT 1")
    suspend fun getMonthlyTargetById(id: String): MonthlyTargetEntity?

    @Query("SELECT * FROM monthly_targets WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingMonthlyTargets(userId: String): List<MonthlyTargetEntity>

    @Upsert
    suspend fun upsertMonthlyTarget(target: MonthlyTargetEntity)

    @Query("UPDATE monthly_targets SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: SyncState)

    @Upsert
    suspend fun upsertMonthlyTargets(targets: List<MonthlyTargetEntity>)

    @Query("DELETE FROM monthly_targets WHERE userId = :userId")
    suspend fun deleteMonthlyTargetsByUserId(userId: String)

    @Query("SELECT * FROM monthly_targets WHERE userId = :userId")
    suspend fun getAllMonthlyTargets(userId: String): List<MonthlyTargetEntity>

    @Query("SELECT COUNT(*) FROM monthly_targets WHERE userId = :userId AND syncStatus != 'SYNCED'")
    fun observePendingCount(userId: String): Flow<Int>
}


