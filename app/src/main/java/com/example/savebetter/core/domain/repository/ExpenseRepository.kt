package com.example.savebetter.core.domain.repository

import com.example.savebetter.core.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Backend-agnostic repository interface for expense transactions.
 *
 * The UI observes Room-backed Flows. All writes save to Room immediately
 * with PENDING sync status, and synchronize with the remote data source asynchronously.
 */
interface ExpenseRepository {
    fun observeExpenses(userId: String): Flow<List<Expense>>
    fun observeExpensesByDateRange(userId: String, startDate: Instant, endDate: Instant): Flow<List<Expense>>
    suspend fun getExpenseById(id: String): Expense?
    suspend fun addExpense(expense: Expense)
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(id: String)
    suspend fun syncPendingExpenses(userId: String): Result<Unit>
}
