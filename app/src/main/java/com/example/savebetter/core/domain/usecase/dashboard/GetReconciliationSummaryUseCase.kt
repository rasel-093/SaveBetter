package com.example.savebetter.core.domain.usecase.dashboard

import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.ReconciliationStatus
import com.example.savebetter.core.domain.model.ReconciliationSummary
import javax.inject.Inject

/**
 * Pure deterministic UseCase calculating month-end cash reconciliation (Screen 07).
 *
 * Formula:
 * - totalExpense = salary - handRemaining
 * - appLoggedExpense = sum(monthly ExpenseEntity)
 * - outOfNoteExpense = totalExpense - appLoggedExpense
 *
 * If outOfNoteExpense < 0:
 * Returns [ReconciliationStatus.DATA_MISMATCH] and clamps outOfNoteExpense to 0
 * to prevent displaying a negative expense to the user.
 */
class GetReconciliationSummaryUseCase @Inject constructor() {

    operator fun invoke(
        year: Int,
        month: Int,
        salaryMinor: Long,
        handRemainingMinor: Long,
        expenses: List<Expense>
    ): ReconciliationSummary {
        val nonDeletedExpenses = expenses.filter { !it.isDeleted }
        val appLoggedExpenseMinor = nonDeletedExpenses.sumOf { it.amountMinor }

        val safeSalary = salaryMinor.coerceAtLeast(0L)
        val safeHandRemaining = handRemainingMinor.coerceAtLeast(0L)

        val totalExpenseMinor = safeSalary - safeHandRemaining
        val rawOutOfNote = totalExpenseMinor - appLoggedExpenseMinor

        val status = when {
            rawOutOfNote < 0L -> ReconciliationStatus.DATA_MISMATCH
            rawOutOfNote == 0L -> ReconciliationStatus.BALANCED
            else -> ReconciliationStatus.UNRECORDED_EXPENSE
        }

        val outOfNoteExpenseMinor = if (status == ReconciliationStatus.DATA_MISMATCH) {
            0L
        } else {
            rawOutOfNote
        }

        return ReconciliationSummary(
            year = year,
            month = month,
            salaryMinor = safeSalary,
            handRemainingMinor = safeHandRemaining,
            totalExpenseMinor = totalExpenseMinor,
            appLoggedExpenseMinor = appLoggedExpenseMinor,
            outOfNoteExpenseMinor = outOfNoteExpenseMinor,
            status = status
        )
    }
}
