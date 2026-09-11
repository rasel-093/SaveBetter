package com.example.savebetter.core.domain.usecase.debt

import com.example.savebetter.core.domain.repository.DebtCreditRepository
import javax.inject.Inject

/**
 * Use case to soft-delete a debt or credit entry.
 */
class DeleteDebtCreditUseCase @Inject constructor(
    private val debtCreditRepository: DebtCreditRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = runCatching {
        debtCreditRepository.deleteDebtCredit(id)
    }
}
