package com.example.savebetter.feature.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.component.SyncStatus
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.usecase.dashboard.GetMonthlySummaryUseCase
import com.example.savebetter.core.domain.usecase.dashboard.GetWeeklySummaryUseCase
import com.example.savebetter.core.domain.usecase.profile.GetUserProfileUseCase
import com.example.savebetter.core.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getWeeklySummaryUseCase: GetWeeklySummaryUseCase,
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val syncManager: SyncManager? = null
) : ViewModel() {

    private val activeUserId = MutableStateFlow<String?>(null)

    fun initForUser(userId: String) {
        if (activeUserId.value != userId) {
            activeUserId.value = userId
        }
    }

    val uiState: StateFlow<HomeUiState> = activeUserId
        .flatMapLatest { userId ->
            if (userId == null) {
                kotlinx.coroutines.flow.flowOf(HomeUiState(isLoading = false))
            } else {
                combine(
                    getUserProfileUseCase(userId),
                    getWeeklySummaryUseCase(userId),
                    getMonthlySummaryUseCase(userId),
                    expenseRepository.observeExpenses(userId),
                    categoryRepository.observeCategories(userId)
                ) { profile, weekly, monthly, expenses, categories ->
                    val now = LocalDate.now()
                    val greeting = getGreetingResId()
                    val monthYear = now.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))

                    val categoryMap = categories.associateBy { it.id }

                    // Sort newest first, take 5 recent expenses
                    val recent = expenses
                        .filter { !it.isDeleted }
                        .sortedByDescending { it.date }
                        .take(5)
                        .map { expense ->
                            val cat = categoryMap[expense.categoryId]
                            val catColor = getCategoryColor(cat?.colorToken)
                            val note = expense.note
                            val title = if (!note.isNullOrBlank()) note else (cat?.customName ?: cat?.nameKey ?: "Expense")
                            val dateStr = expense.date.atZone(ZoneId.systemDefault()).toLocalDate().toString()

                            ExpenseItemUiModel(
                                id = expense.id,
                                title = title,
                                categoryName = cat?.customName ?: cat?.nameKey ?: "General",
                                categoryColor = catColor,
                                amountMinor = expense.amountMinor,
                                date = dateStr,
                                note = note
                            )
                        }

                    val syncStatus = when {
                        profile?.syncStatus == SyncState.PENDING ||
                                expenses.any { it.syncStatus == SyncState.PENDING } ||
                                weekly.spentAmountMinor > 0 && profile?.syncStatus == SyncState.PENDING -> SyncStatus.Offline
                        profile?.syncStatus == SyncState.SYNCING -> SyncStatus.Syncing
                        else -> SyncStatus.Synced
                    }

                    HomeUiState(
                        userProfile = profile,
                        weeklySummary = weekly,
                        monthlySummary = monthly,
                        recentExpenses = recent,
                        syncStatus = syncStatus,
                        greetingResId = greeting,
                        monthYearText = monthYear,
                        isLoading = false
                    )
                }.combine(syncManager?.syncStatus ?: flowOf(null)) { state, liveStatus ->
                    if (liveStatus != null) state.copy(syncStatus = liveStatus) else state
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true)
        )

    private fun getGreetingResId(): Int {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 5..11 -> R.string.greeting_morning
            in 12..16 -> R.string.greeting_afternoon
            in 17..21 -> R.string.greeting_evening
            else -> R.string.greeting_default
        }
    }

    private fun getCategoryColor(colorToken: String?): Color {
        return when (colorToken) {
            "cat1" -> Color(0xFFC9A227)
            "cat2" -> Color(0xFFA23E32)
            "cat3" -> Color(0xFF2F6F62)
            "cat4" -> Color(0xFF4C6785)
            "cat5" -> Color(0xFF7C8C3E)
            "cat6" -> Color(0xFF9C8F73)
            else -> Color(0xFFC9A227)
        }
    }
}
