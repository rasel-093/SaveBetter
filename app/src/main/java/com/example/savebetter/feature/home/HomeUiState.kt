package com.example.savebetter.feature.home

import androidx.compose.ui.graphics.Color
import com.example.savebetter.core.designsystem.component.SyncStatus
import com.example.savebetter.core.domain.model.MonthlySummary
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklySummary

/**
 * UI presentation model for an expense list item in the dashboard.
 */
data class ExpenseItemUiModel(
    val id: String,
    val title: String,
    val categoryName: String,
    val categoryColor: Color,
    val amountMinor: Long,
    val date: String,
    val note: String? = null
)

/**
 * UI state for the Home Dashboard (Screen 03).
 */
data class HomeUiState(
    val userProfile: UserProfile? = null,
    val weeklySummary: WeeklySummary = WeeklySummary(),
    val monthlySummary: MonthlySummary = MonthlySummary(),
    val recentExpenses: List<ExpenseItemUiModel> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.Synced,
    val greetingResId: Int = com.example.savebetter.R.string.greeting_morning,
    val monthYearText: String = "",
    val isLoading: Boolean = true
)
