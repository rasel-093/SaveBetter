package com.example.savebetter.core.domain.usecase.dashboard

import com.example.savebetter.core.domain.model.WeeklySummary
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Use case to compute real-time weekly budget progress and metrics for the dashboard.
 *
 * Warning rules:
 * - < 80%  -> Normal (moss)
 * - >= 80% -> Warning (gold)
 * - >= 100% -> Over budget (brick)
 */
class GetWeeklySummaryUseCase @Inject constructor(
    private val targetRepository: TargetRepository,
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(userId: String, referenceDate: LocalDate = LocalDate.now()): Flow<WeeklySummary> {
        val weekStartDate = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEndDate = referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val weekStart = weekStartDate.toString()
        val weekEnd = weekEndDate.toString()

        return combine(
            targetRepository.observeWeeklyTargets(userId),
            expenseRepository.observeExpenses(userId)
        ) { targets, expenses ->
            val currentTarget = targets.find { it.weekStart == weekStart }
                ?: targets.maxByOrNull { it.weekStart }

            val targetAmountMinor = currentTarget?.targetAmountMinor ?: 0L

            val weeklyExpenses = expenses.filter { expense ->
                if (expense.isDeleted) return@filter false
                val expenseDate = expense.date.atZone(ZoneId.systemDefault()).toLocalDate()
                !expenseDate.isBefore(weekStartDate) && !expenseDate.isAfter(weekEndDate)
            }

            val spentAmountMinor = weeklyExpenses.sumOf { it.amountMinor }
            val remainingAmountMinor = maxOf(0L, targetAmountMinor - spentAmountMinor)
            val percentage = if (targetAmountMinor > 0) spentAmountMinor.toFloat() / targetAmountMinor else 0f
            val isWarning = percentage >= 0.80f
            val isOverBudget = percentage >= 1.00f

            val dailyLimitMinor = if (targetAmountMinor > 0) targetAmountMinor / 7L else 0L
            val dailySpentMinor = weeklyExpenses
                .filter {
                    val expenseDate = it.date.atZone(ZoneId.systemDefault()).toLocalDate()
                    expenseDate == referenceDate
                }
                .sumOf { it.amountMinor }

            WeeklySummary(
                targetAmountMinor = targetAmountMinor,
                spentAmountMinor = spentAmountMinor,
                remainingAmountMinor = remainingAmountMinor,
                percentage = percentage,
                dailyLimitMinor = dailyLimitMinor,
                dailySpentMinor = dailySpentMinor,
                isWarning = isWarning,
                isOverBudget = isOverBudget,
                weekStart = weekStart,
                weekEnd = weekEnd
            )
        }
    }
}
