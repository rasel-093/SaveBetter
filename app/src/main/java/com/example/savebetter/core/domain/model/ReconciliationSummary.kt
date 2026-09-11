package com.example.savebetter.core.domain.model

/**
 * Reconciliation discrepancy status.
 */
enum class ReconciliationStatus {
    /** Out-of-note expense is 0: perfectly reconciled */
    BALANCED,

    /** Total net cash outflow > recorded expenses: unrecorded spending detected */
    UNRECORDED_EXPENSE,

    /**
     * Cash in hand + logged expenses > salary / starting cash.
     * Indicates data mismatch rather than displaying a negative expense.
     */
    DATA_MISMATCH
}

/**
 * Domain model representing Screen 07 month-end cash reconciliation.
 *
 * Formula:
 * - totalExpense = salary - handRemaining
 * - appLoggedExpense = sum(monthly ExpenseEntity)
 * - outOfNoteExpense = totalExpense - appLoggedExpense
 */
data class ReconciliationSummary(
    val year: Int,
    val month: Int,
    val salaryMinor: Long,
    val handRemainingMinor: Long,
    val totalExpenseMinor: Long,
    val appLoggedExpenseMinor: Long,
    val outOfNoteExpenseMinor: Long,
    val status: ReconciliationStatus
)
