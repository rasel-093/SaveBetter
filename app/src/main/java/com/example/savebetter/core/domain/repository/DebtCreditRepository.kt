package com.example.savebetter.core.domain.repository

import com.example.savebetter.core.domain.model.DebtCredit
import kotlinx.coroutines.flow.Flow

/**
 * Backend-agnostic repository interface for debts (payable) and credits (receivable).
 */
interface DebtCreditRepository {
    fun observeDebtsAndCredits(userId: String): Flow<List<DebtCredit>>
    suspend fun getDebtCreditById(id: String): DebtCredit?
    suspend fun addDebtCredit(item: DebtCredit)
    suspend fun updateDebtCredit(item: DebtCredit)
    suspend fun markSettled(id: String, isSettled: Boolean)
    suspend fun deleteDebtCredit(id: String)
    suspend fun syncPendingDebtsAndCredits(userId: String): Result<Unit>
}
