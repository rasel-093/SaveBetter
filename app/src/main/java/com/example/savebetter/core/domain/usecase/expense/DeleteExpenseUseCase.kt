package com.example.savebetter.core.domain.usecase.expense

import com.example.savebetter.core.domain.repository.ExpenseRepository
import javax.inject.Inject

/**
 * Use case to delete an expense.
 *
 * Implements soft-delete with a tombstone (isDeleted = true, syncStatus = PENDING)
 * so that deletions are safely propagated across devices during synchronization.
 */
class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(expenseId: String): Result<Unit> = runCatching {
        expenseRepository.deleteExpense(expenseId)
    }
}
