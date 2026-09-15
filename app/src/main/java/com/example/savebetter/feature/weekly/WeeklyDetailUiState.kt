package com.example.savebetter.feature.weekly

import com.example.savebetter.core.designsystem.component.DailyBarData
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.WeeklyAdvice
import com.example.savebetter.core.domain.model.WeeklySummary
import com.example.savebetter.feature.home.ExpenseItemUiModel
import java.time.LocalDate

/**
 * UI State for Screen 05: Weekly Detail.
 */
data class WeeklyDetailUiState(
    val weeklySummary: WeeklySummary = WeeklySummary(
        targetAmountMinor = 0L,
        spentAmountMinor = 0L,
        remainingAmountMinor = 0L,
        percentage = 0f,
        dailyLimitMinor = 0L,
        dailySpentMinor = 0L,
        isWarning = false,
        isOverBudget = false,
        weekStart = "",
        weekEnd = ""
    ),
    val weekDateRangeText: String = "",
    val referenceDate: LocalDate = LocalDate.now(),
    val dailyTrendBars: List<DailyBarData> = emptyList(),
    val adviceList: List<WeeklyAdvice> = emptyList(),
    val weeklyExpenses: List<ExpenseItemUiModel> = emptyList(),
    val categories: List<Category> = emptyList(),
    val hasNextWeek: Boolean = false,
    val showBudgetDialog: Boolean = false,
    val userMessage: String? = null,
    val isLoading: Boolean = true
)
