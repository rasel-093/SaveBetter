package com.example.savebetter.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.savebetter.core.data.local.entity.WeeklyTargetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for the WeeklyTarget entity.
 */
@Dao
interface WeeklyTargetDao {

    @Query("SELECT * FROM weekly_targets WHERE userId = :userId ORDER BY weekStart DESC")
    fun observeWeeklyTargets(userId: String): Flow<List<WeeklyTargetEntity>>

    @Query("SELECT * FROM weekly_targets WHERE userId = :userId AND :date >= weekStart AND :date <= weekEnd LIMIT 1")
    suspend fun getWeeklyTargetForDate(userId: String, date: String): WeeklyTargetEntity?

    @Query("SELECT * FROM weekly_targets WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingWeeklyTargets(userId: String): List<WeeklyTargetEntity>

    @Upsert
    suspend fun upsertWeeklyTarget(target: WeeklyTargetEntity)

    @Upsert
    suspend fun upsertWeeklyTargets(targets: List<WeeklyTargetEntity>)
}
