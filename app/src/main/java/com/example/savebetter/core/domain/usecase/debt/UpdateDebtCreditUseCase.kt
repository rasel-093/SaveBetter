package com.example.savebetter.core.domain.usecase.debt

import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.DebtCreditRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Use case to update an existing debt or credit entry.
 */
class UpdateDebtCreditUseCase @Inject constructor(
    private val debtCreditRepository: DebtCreditRepository
) {
    suspend operator fun invoke(item: DebtCredit): Result<Unit> {
        val trimmedName = item.personName.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Person name cannot be blank"))
        }
        if (item.amountMinor <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }

        val updated = item.copy(
            personName = trimmedName,
            note = item.note?.trim()?.takeIf { it.isNotEmpty() },
            updatedAt = Instant.now(),
            syncStatus = SyncState.PENDING
        )

        return runCatching {
            debtCreditRepository.updateDebtCredit(updated)
        }
    }
}
