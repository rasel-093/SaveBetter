package com.example.savebetter.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.savebetter.core.data.local.entity.MonthlyTargetEntity
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

    @Query("SELECT * FROM monthly_targets WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingMonthlyTargets(userId: String): List<MonthlyTargetEntity>

    @Upsert
    suspend fun upsertMonthlyTarget(target: MonthlyTargetEntity)

    @Upsert
    suspend fun upsertMonthlyTargets(targets: List<MonthlyTargetEntity>)
}
