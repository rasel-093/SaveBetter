package com.example.savebetter.core.domain.model

/**
 * Summary metrics for the active week.
 */
data class WeeklySummary(
    val targetAmountMinor: Long = 0L,
    val spentAmountMinor: Long = 0L,
    val remainingAmountMinor: Long = 0L,
    val percentage: Float = 0f,
    val dailyLimitMinor: Long = 0L,
    val dailySpentMinor: Long = 0L,
    val isWarning: Boolean = false,
    val isOverBudget: Boolean = false,
    val weekStart: String = "",
    val weekEnd: String = ""
)

/**
 * Summary metrics for the active month.
 */
data class MonthlySummary(
    val month: Int = 1,
    val year: Int = 2026,
    val targetAmountMinor: Long = 0L,
    val savingGoalMinor: Long = 0L,
    val spentAmountMinor: Long = 0L,
    val remainingAmountMinor: Long = 0L,
    val salaryAmountMinor: Long = 0L,
    val handRemainingMinor: Long = 0L,
    val projectedSpentMinor: Long = 0L,
    val percentage: Float = 0f,
    val isWarning: Boolean = false,
    val isOverBudget: Boolean = false
)
