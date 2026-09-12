package com.example.savebetter.feature.monthly

import com.example.savebetter.core.designsystem.component.ComparisonBarItem
import com.example.savebetter.core.designsystem.component.DonutSlice
import com.example.savebetter.core.domain.model.MonthlySuggestion
import java.time.YearMonth

/**
 * UI state for Screen 06: Monthly Analysis (মাসিক বিশ্লেষণ).
 */
data class MonthlyAnalysisUiState(
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val monthTitleText: String = "",
    val targetAmountMinor: Long = 0L,
    val spentAmountMinor: Long = 0L,
    val savingGoalMinor: Long = 0L,
    val budgetProgress: Float = 0f,
    val isOverBudget: Boolean = false,
    val isWarning: Boolean = false,
    val categorySlices: List<DonutSlice> = emptyList(),
    val suggestions: List<MonthlySuggestion> = emptyList(),
    val comparisonPrevious: ComparisonBarItem? = null,
    val comparisonCurrent: ComparisonBarItem? = null,
    val hasNextMonth: Boolean = false,
    val isLoading: Boolean = true
)
