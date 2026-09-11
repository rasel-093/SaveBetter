package com.example.savebetter.core.domain.usecase.debt

import com.example.savebetter.core.domain.repository.DebtCreditRepository
import javax.inject.Inject

/**
 * Use case to mark a debt or credit entry as settled or unsettled.
 * Settled entries remain in history.
 */
class SettleDebtCreditUseCase @Inject constructor(
    private val debtCreditRepository: DebtCreditRepository
) {
    suspend operator fun invoke(id: String, isSettled: Boolean): Result<Unit> = runCatching {
        debtCreditRepository.markSettled(id, isSettled)
    }
}
