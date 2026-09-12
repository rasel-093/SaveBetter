package com.example.savebetter.feature.reconciliation

import androidx.annotation.StringRes
import com.example.savebetter.core.domain.model.ReconciliationStatus
import com.example.savebetter.core.domain.model.ReconciliationSummary
import com.example.savebetter.core.domain.model.SyncState
import java.time.YearMonth

/**
 * UI State for Screen 07: Month-End Cash Reconciliation (মাস শেষের মিলান).
 */
data class ReconciliationUiState(
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val monthDisplay: String = "",
    val salaryInput: String = "",
    val handRemainingInput: String = "",
    val summary: ReconciliationSummary = ReconciliationSummary(
        year = YearMonth.now().year,
        month = YearMonth.now().monthValue,
        salaryMinor = 0L,
        handRemainingMinor = 0L,
        totalExpenseMinor = 0L,
        appLoggedExpenseMinor = 0L,
        outOfNoteExpenseMinor = 0L,
        status = ReconciliationStatus.BALANCED
    ),
    val formattedSalary: String = "৳০",
    val formattedHandRemaining: String = "৳০",
    val formattedNetOutflow: String = "৳০",
    val formattedAppLogged: String = "৳০",
    val formattedUnrecorded: String = "৳০",
    val isSaving: Boolean = false,
    val showQuickLogDialog: Boolean = false,
    val syncStatus: SyncState = SyncState.SYNCED,
    val liveSyncStatus: com.example.savebetter.core.designsystem.component.SyncStatus? = null,
    val isCurrentMonth: Boolean = true,
    @StringRes val userMessageResId: Int? = null
)
