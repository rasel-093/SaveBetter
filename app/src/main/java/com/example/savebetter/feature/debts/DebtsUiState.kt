package com.example.savebetter.feature.debts

import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtCreditSummary

enum class DebtTab {
    ACTIVE,
    HISTORY
}

data class DebtsUiState(
    val isLoading: Boolean = true,
    val summary: DebtCreditSummary = DebtCreditSummary(),
    val selectedTab: DebtTab = DebtTab.ACTIVE,
    val isAddEditSheetOpen: Boolean = false,
    val editingItem: DebtCredit? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)
