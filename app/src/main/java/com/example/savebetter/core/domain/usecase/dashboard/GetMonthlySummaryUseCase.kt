package com.example.savebetter.core.domain.usecase.dashboard

import com.example.savebetter.core.domain.model.MonthlySummary
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Use case to compute real-time monthly budget metrics, savings progress,
 * and spending pace projection for the dashboard.
 */
class GetMonthlySummaryUseCase @Inject constructor(
    private val targetRepository: TargetRepository,
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(userId: String, referenceDate: LocalDate = LocalDate.now()): Flow<MonthlySummary> {
        val month = referenceDate.monthValue
        val year = referenceDate.year
        val daysInMonth = referenceDate.lengthOfMonth()
        val dayOfMonth = referenceDate.dayOfMonth

        return combine(
            targetRepository.observeMonthlyTargets(userId),
            targetRepository.observeSalaryHandRecords(userId),
            expenseRepository.observeExpenses(userId)
        ) { targets, salaryRecords, expenses ->
            val currentTarget = targets.find { it.month == month && it.year == year }
                ?: targets.maxByOrNull { it.year * 100 + it.month }

            val targetAmountMinor = currentTarget?.targetAmountMinor ?: 0L
            val savingGoalMinor = currentTarget?.savingGoalMinor ?: 0L

            val currentSalary = salaryRecords.find { it.month == month && it.year == year }
                ?: salaryRecords.maxByOrNull { it.year * 100 + it.month }
            val salaryAmountMinor = currentSalary?.salaryAmountMinor ?: 0L

            // Filter expenses for current month and year
            val monthlyExpenses = expenses.filter { expense ->
                if (expense.isDeleted) return@filter false
                try {
                    val expenseDate = expense.date.atZone(ZoneId.systemDefault()).toLocalDate()
                    expenseDate.year == year && expenseDate.monthValue == month
                } catch (_: Exception) {
                    false
                }
            }

            val spentAmountMinor = monthlyExpenses.sumOf { it.amountMinor }
            val remainingAmountMinor = maxOf(0L, targetAmountMinor - spentAmountMinor)
            val handRemainingMinor = maxOf(0L, salaryAmountMinor - spentAmountMinor)
            val percentage = if (targetAmountMinor > 0) spentAmountMinor.toFloat() / targetAmountMinor else 0f
            val isWarning = percentage >= 0.80f
            val isOverBudget = percentage >= 1.00f

            // Pace projection: daily average spent multiplied by total days in month
            val dailyAverage = if (dayOfMonth > 0) spentAmountMinor / dayOfMonth else 0L
            val projectedSpentMinor = dailyAverage * daysInMonth

            MonthlySummary(
                month = month,
                year = year,
                targetAmountMinor = targetAmountMinor,
                savingGoalMinor = savingGoalMinor,
                spentAmountMinor = spentAmountMinor,
                remainingAmountMinor = remainingAmountMinor,
                salaryAmountMinor = salaryAmountMinor,
                handRemainingMinor = handRemainingMinor,
                projectedSpentMinor = projectedSpentMinor,
                percentage = percentage,
                isWarning = isWarning,
                isOverBudget = isOverBudget
            )
        }
    }
}
