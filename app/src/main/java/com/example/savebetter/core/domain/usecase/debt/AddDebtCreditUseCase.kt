package com.example.savebetter.core.domain.usecase.debt

import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.DebtCreditRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to create a new debt or credit entry.
 */
class AddDebtCreditUseCase @Inject constructor(
    private val debtCreditRepository: DebtCreditRepository
) {
    suspend operator fun invoke(
        userId: String,
        direction: DebtDirection,
        personName: String,
        amountMinor: Long,
        note: String? = null,
        date: Instant = Instant.now(),
        dueDate: Instant? = null
    ): Result<DebtCredit> {
        val trimmedName = personName.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Person name cannot be blank"))
        }
        if (amountMinor <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }

        val item = DebtCredit(
            id = UUID.randomUUID().toString(),
            userId = userId,
            direction = direction,
            personName = trimmedName,
            amountMinor = amountMinor,
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            date = date,
            dueDate = dueDate,
            isSettled = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            syncStatus = SyncState.PENDING,
            isDeleted = false,
            deletedAt = null
        )

        return runCatching {
            debtCreditRepository.addDebtCredit(item)
            item
        }
    }
}
