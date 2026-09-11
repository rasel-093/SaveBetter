package com.example.savebetter.core.domain.model

import java.time.Instant

/**
 * Weekly expense target budget.
 */
data class WeeklyTarget(
    val id: String,
    val userId: String,
    val weekStart: String,
    val weekEnd: String,
    val targetAmountMinor: Long,
    val updatedAt: Instant = Instant.now(),
    val syncStatus: SyncState = SyncState.SYNCED
)

/**
 * Monthly expense target and saving goal.
 */
data class MonthlyTarget(
    val id: String,
    val userId: String,
    val month: Int,
    val year: Int,
    val targetAmountMinor: Long,
    val savingGoalMinor: Long = 0L,
    val updatedAt: Instant = Instant.now(),
    val syncStatus: SyncState = SyncState.SYNCED
)

/**
 * Monthly salary and cash-in-hand tracking record.
 */
data class SalaryHandRecord(
    val id: String,
    val userId: String,
    val month: Int,
    val year: Int,
    val salaryAmountMinor: Long,
    val handRemainingAmountMinor: Long,
    val updatedAt: Instant = Instant.now(),
    val syncStatus: SyncState = SyncState.SYNCED
)
