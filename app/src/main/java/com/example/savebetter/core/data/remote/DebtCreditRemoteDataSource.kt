package com.example.savebetter.core.data.remote

import com.example.savebetter.core.domain.model.DebtCredit

/**
 * Backend-agnostic remote data source interface for debts and credits.
 */
interface DebtCreditRemoteDataSource {
    suspend fun fetchDebtsAndCredits(userId: String): Result<List<DebtCredit>>
    suspend fun uploadDebtCredit(item: DebtCredit): Result<Unit>
    suspend fun deleteDebtCredit(userId: String, id: String): Result<Unit>
}
