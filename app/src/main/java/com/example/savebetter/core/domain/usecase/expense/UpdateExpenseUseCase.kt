package com.example.savebetter.core.domain.usecase.expense

import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.ExpenseRepository
import java.time.Instant
import javax.inject.Inject

data class UpdateExpenseParams(
    val id: String,
    val userId: String,
    val amountMinor: Long,
    val categoryId: String,
    val date: Instant,
    val note: String? = null
)

/**
 * Use case to update an existing expense.
 *
 * Saves immediately to Room with SyncState.PENDING so the UI updates with zero latency.
 */
class UpdateExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(params: UpdateExpenseParams): Result<Unit> = runCatching {
        val existing = expenseRepository.getExpenseById(params.id)
            ?: throw IllegalArgumentException("Expense not found with ID: ${params.id}")

        val updated = existing.copy(
            amountMinor = params.amountMinor,
            categoryId = params.categoryId,
            note = params.note?.trim()?.ifBlank { null },
            date = params.date,
            updatedAt = Instant.now(),
            syncStatus = SyncState.PENDING
        )
        expenseRepository.updateExpense(updated)
    }
}
