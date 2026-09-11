package com.example.savebetter.core.domain.usecase.debt

import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtCreditSummary
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.repository.DebtCreditRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case to compute real-time summary statistics for Debts and Credits.
 *
 * Rules:
 * - Active items (!isSettled) are categorized into Receivables (Lent) and Payables (Borrowed).
 * - Settled items remain in history and are excluded from active totals.
 * - Net position = Total Receivable - Total Payable.
 */
class GetDebtsAndCreditsSummaryUseCase @Inject constructor(
    private val debtCreditRepository: DebtCreditRepository
) {
    operator fun invoke(userId: String): Flow<DebtCreditSummary> {
        return debtCreditRepository.observeDebtsAndCredits(userId).map { items ->
            val nonDeleted = items.filterNot { it.isDeleted }
            val active = nonDeleted.filterNot { it.isSettled }
            val settled = nonDeleted.filter { it.isSettled }

            val activeReceivables = active.filter { it.direction == DebtDirection.RECEIVABLE }
            val activePayables = active.filter { it.direction == DebtDirection.PAYABLE }

            val totalReceivableMinor = activeReceivables.sumOf { it.amountMinor }
            val totalPayableMinor = activePayables.sumOf { it.amountMinor }
            val netPositionMinor = totalReceivableMinor - totalPayableMinor

            DebtCreditSummary(
                totalReceivableMinor = totalReceivableMinor,
                totalPayableMinor = totalPayableMinor,
                netPositionMinor = netPositionMinor,
                activeReceivables = activeReceivables,
                activePayables = activePayables,
                settledList = settled
            )
        }
    }
}
