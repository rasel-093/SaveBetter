package com.example.savebetter.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.savebetter.core.data.local.entity.ExpenseEntity
import com.example.savebetter.core.domain.model.SyncState
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Data access object for the Expense entity.
 */
@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE userId = :userId AND isDeleted = 0 ORDER BY date DESC")
    fun observeExpenses(userId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date >= :startDate AND date <= :endDate AND isDeleted = 0 ORDER BY date DESC")
    fun observeExpensesByDateRange(userId: String, startDate: Instant, endDate: Instant): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getExpenseById(id: String): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingExpenses(userId: String): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Upsert
    suspend fun upsertExpenses(expenses: List<ExpenseEntity>)

    @Query("UPDATE expenses SET isDeleted = 1, deletedAt = :deletedAt, syncStatus = :syncStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteExpense(id: String, deletedAt: Instant, updatedAt: Instant, syncStatus: SyncState)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun hardDeleteExpense(id: String)

    @Query("DELETE FROM expenses WHERE userId = :userId")
    suspend fun deleteExpensesByUserId(userId: String)
}

