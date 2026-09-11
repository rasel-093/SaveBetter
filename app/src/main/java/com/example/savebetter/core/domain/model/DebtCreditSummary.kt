package com.example.savebetter.core.domain.model

/**
 * Aggregated domain model for debts & credits summary calculations.
 */
data class DebtCreditSummary(
    val totalReceivableMinor: Long = 0L,
    val totalPayableMinor: Long = 0L,
    val netPositionMinor: Long = 0L,
    val activeReceivables: List<DebtCredit> = emptyList(),
    val activePayables: List<DebtCredit> = emptyList(),
    val settledList: List<DebtCredit> = emptyList()
)
