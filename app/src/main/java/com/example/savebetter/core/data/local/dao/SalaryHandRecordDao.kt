package com.example.savebetter.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.savebetter.core.data.local.entity.SalaryHandRecordEntity
import com.example.savebetter.core.domain.model.SyncState
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for the SalaryHandRecord entity.
 */
@Dao
interface SalaryHandRecordDao {

    @Query("SELECT * FROM salary_hand_records WHERE userId = :userId ORDER BY year DESC, month DESC")
    fun observeSalaryHandRecords(userId: String): Flow<List<SalaryHandRecordEntity>>

    @Query("SELECT * FROM salary_hand_records WHERE userId = :userId AND year = :year AND month = :month LIMIT 1")
    suspend fun getSalaryHandRecord(userId: String, year: Int, month: Int): SalaryHandRecordEntity?

    @Query("SELECT * FROM salary_hand_records WHERE id = :id LIMIT 1")
    suspend fun getSalaryHandRecordById(id: String): SalaryHandRecordEntity?

    @Query("SELECT * FROM salary_hand_records WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingSalaryHandRecords(userId: String): List<SalaryHandRecordEntity>

    @Upsert
    suspend fun upsertSalaryHandRecord(record: SalaryHandRecordEntity)

    @Query("UPDATE salary_hand_records SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: SyncState)

    @Upsert
    suspend fun upsertSalaryHandRecords(records: List<SalaryHandRecordEntity>)

    @Query("DELETE FROM salary_hand_records WHERE userId = :userId")
    suspend fun deleteSalaryHandRecordsByUserId(userId: String)

    @Query("SELECT * FROM salary_hand_records WHERE userId = :userId")
    suspend fun getAllSalaryHandRecords(userId: String): List<SalaryHandRecordEntity>

    @Query("SELECT COUNT(*) FROM salary_hand_records WHERE userId = :userId AND syncStatus != 'SYNCED'")
    fun observePendingCount(userId: String): Flow<Int>
}


