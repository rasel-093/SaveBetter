package com.example.savebetter.core.domain.usecase.dashboard

import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.TargetRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * Saves or updates the salary and cash-in-hand record for a specific month.
 * Commits immediately to local Room database with [SyncState.PENDING].
 */
class SaveSalaryHandRecordUseCase @Inject constructor(
    private val targetRepository: TargetRepository
) {
    suspend operator fun invoke(
        userId: String,
        year: Int,
        month: Int,
        salaryAmountMinor: Long,
        handRemainingAmountMinor: Long
    ): Result<SalaryHandRecord> {
        return try {
            val existing = targetRepository.getSalaryHandRecord(userId, year, month)
            val record = existing?.copy(
                salaryAmountMinor = salaryAmountMinor,
                handRemainingAmountMinor = handRemainingAmountMinor,
                updatedAt = Instant.now(),
                syncStatus = SyncState.PENDING
            ) ?: SalaryHandRecord(
                id = UUID.randomUUID().toString(),
                userId = userId,
                year = year,
                month = month,
                salaryAmountMinor = salaryAmountMinor,
                handRemainingAmountMinor = handRemainingAmountMinor,
                updatedAt = Instant.now(),
                syncStatus = SyncState.PENDING
            )

            targetRepository.saveSalaryHandRecord(record)
            Result.success(record)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
