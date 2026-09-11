package com.example.savebetter.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.savebetter.core.data.local.entity.DebtCreditEntity
import com.example.savebetter.core.domain.model.SyncState
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Data access object for the DebtCredit entity.
 */
@Dao
interface DebtCreditDao {

    @Query("SELECT * FROM debt_credits WHERE userId = :userId AND isDeleted = 0 ORDER BY date DESC")
    fun observeDebtsAndCredits(userId: String): Flow<List<DebtCreditEntity>>

    @Query("SELECT * FROM debt_credits WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getDebtCreditById(id: String): DebtCreditEntity?

    @Query("SELECT * FROM debt_credits WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingDebtsAndCredits(userId: String): List<DebtCreditEntity>

    @Upsert
    suspend fun upsertDebtCredit(item: DebtCreditEntity)

    @Upsert
    suspend fun upsertDebtsAndCredits(items: List<DebtCreditEntity>)

    @Query("UPDATE debt_credits SET isSettled = :isSettled, updatedAt = :updatedAt, syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSettled(id: String, isSettled: Boolean, updatedAt: Instant, syncStatus: SyncState)

    @Query("UPDATE debt_credits SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :updatedAt, syncStatus = :syncStatus WHERE id = :id")
    suspend fun softDeleteDebtCredit(id: String, deletedAt: Instant, updatedAt: Instant, syncStatus: SyncState)
}
