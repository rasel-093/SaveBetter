package com.example.savebetter.core.domain.usecase.expense

import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.ExpenseRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class AddExpenseParams(
    val userId: String,
    val amountMinor: Long,
    val categoryId: String,
    val date: Instant,
    val note: String? = null
)

/**
 * Use case to record a new expense.
 *
 * Saves immediately to Room with SyncState.PENDING so the UI updates with zero latency.
 */
class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(params: AddExpenseParams): Result<Expense> = runCatching {
        val now = Instant.now()
        val expense = Expense(
            id = UUID.randomUUID().toString(),
            userId = params.userId,
            amountMinor = params.amountMinor,
            categoryId = params.categoryId,
            note = params.note?.trim()?.ifBlank { null },
            date = params.date,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncState.PENDING
        )
        expenseRepository.addExpense(expense)
        expense
    }
}
