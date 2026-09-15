package com.example.savebetter.feature.monthly

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebetter.core.designsystem.component.ComparisonBarItem
import com.example.savebetter.core.designsystem.component.DonutSlice
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import com.example.savebetter.core.domain.usecase.dashboard.GetMonthlyReductionSuggestionsUseCase
import com.example.savebetter.core.domain.usecase.dashboard.GetMonthlySummaryUseCase
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.i18n.CurrencyFormatter
import com.example.savebetter.core.i18n.NumeralConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MonthlyAnalysisViewModel @Inject constructor(
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    private val getMonthlyReductionSuggestionsUseCase: GetMonthlyReductionSuggestionsUseCase,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val targetRepository: TargetRepository
) : ViewModel() {

    private val activeUserId = MutableStateFlow<String?>(null)
    private val selectedYearMonth = MutableStateFlow(YearMonth.now())
    private val currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    private val showBudgetDialogState = MutableStateFlow(false)
    private val userMessageState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MonthlyAnalysisUiState> = combine(
        activeUserId,
        selectedYearMonth,
        currentLanguage,
        showBudgetDialogState,
        userMessageState
    ) { userId, ym, language, showDialog, userMsg ->
        StateParams(userId, ym, language, showDialog, userMsg)
    }.flatMapLatest { params ->
        val userId = params.userId
        val ym = params.ym
        val language = params.language
        val showDialog = params.showDialog
        val userMsg = params.userMsg

        if (userId == null) {
            flowOf(MonthlyAnalysisUiState(isLoading = false, showBudgetDialog = showDialog, userMessage = userMsg))
        } else {
            val prevYm = ym.minusMonths(1)

            combine(
                getMonthlySummaryUseCase(userId, ym.atDay(1)),
                expenseRepository.observeExpenses(userId),
                categoryRepository.observeCategories(userId)
            ) { monthlySummary, allExpenses, categories ->
                val activeCategories = categories.filter { !it.isDeleted }
                val categoryMap = activeCategories.associateBy { it.id }

                // 1. Filter current and previous month expenses
                val currentExpenses = allExpenses.filter { expense ->
                    if (expense.isDeleted) return@filter false
                    val date = expense.date.atZone(ZoneId.systemDefault()).toLocalDate()
                    date.year == ym.year && date.monthValue == ym.monthValue
                }

                val previousExpenses = allExpenses.filter { expense ->
                    if (expense.isDeleted) return@filter false
                    val date = expense.date.atZone(ZoneId.systemDefault()).toLocalDate()
                    date.year == prevYm.year && date.monthValue == prevYm.monthValue
                }

                // 2. Build Category Donut Slices
                val currentCategoryTotals = currentExpenses
                    .groupBy { it.categoryId }
                    .mapValues { (_, exps) -> exps.sumOf { it.amountMinor } }

                val totalCurrentSpend = currentExpenses.sumOf { it.amountMinor }

                val donutSlices = currentCategoryTotals.entries
                    .sortedByDescending { it.value }
                    .map { (catId, amountMinor) ->
                        val cat = categoryMap[catId]
                        val catName = cat?.customName ?: cat?.nameKey ?: "Category"
                        val catColor = getCategoryColor(cat?.colorToken)
                        DonutSlice(
                            label = catName,
                            value = amountMinor / 100f,
                            color = catColor
                        )
                    }

                // 3. Build Month-over-Month Comparison Bars
                val prevTotalSpend = previousExpenses.sumOf { it.amountMinor }
                val prevLabel = formatMonthLabel(prevYm, language)
                val currentLabel = formatMonthLabel(ym, language)

                val comparisonPrev = ComparisonBarItem(
                    label = prevLabel,
                    amountFormatted = CurrencyFormatter.formatMinor(prevTotalSpend, language),
                    value = prevTotalSpend / 100f,
                    color = Color(0xFF9C8F73) // Slate / cat6
                )

                val currentBarColor = when {
                    monthlySummary.isOverBudget -> Color(0xFFA23E32) // brick
                    monthlySummary.isWarning -> Color(0xFFC9A227) // gold
                    else -> Color(0xFF3F7856) // moss
                }

                val comparisonCurrent = ComparisonBarItem(
                    label = currentLabel,
                    amountFormatted = CurrencyFormatter.formatMinor(totalCurrentSpend, language),
                    value = totalCurrentSpend / 100f,
                    color = currentBarColor
                )

                // 4. Generate intelligent suggestions (with outlier exclusion)
                val suggestions = getMonthlyReductionSuggestionsUseCase(
                    currentMonthExpenses = currentExpenses,
                    previousMonthExpenses = previousExpenses,
                    categories = activeCategories,
                    monthlySummary = monthlySummary,
                    language = language
                )

                val monthTitle = formatMonthTitle(ym, language)
                val currentYm = YearMonth.now()
                val hasNextMonth = ym.isBefore(currentYm)

                MonthlyAnalysisUiState(
                    selectedYearMonth = ym,
                    monthTitleText = monthTitle,
                    targetAmountMinor = monthlySummary.targetAmountMinor,
                    spentAmountMinor = totalCurrentSpend,
                    savingGoalMinor = monthlySummary.savingGoalMinor,
                    budgetProgress = monthlySummary.percentage,
                    isOverBudget = monthlySummary.isOverBudget,
                    isWarning = monthlySummary.isWarning,
                    categorySlices = donutSlices,
                    suggestions = suggestions,
                    comparisonPrevious = comparisonPrev,
                    comparisonCurrent = comparisonCurrent,
                    hasNextMonth = hasNextMonth,
                    showBudgetDialog = showDialog,
                    userMessage = userMsg,
                    isLoading = false
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MonthlyAnalysisUiState(isLoading = true)
    )

    private data class StateParams(
        val userId: String?,
        val ym: YearMonth,
        val language: AppLanguage,
        val showDialog: Boolean,
        val userMsg: String?
    )

    fun initForUser(userId: String, language: AppLanguage = AppLanguage.ENGLISH) {
        activeUserId.value = userId
        currentLanguage.value = language
    }

    fun setLanguage(language: AppLanguage) {
        currentLanguage.value = language
    }

    fun showBudgetDialog(show: Boolean) {
        showBudgetDialogState.value = show
    }

    fun saveMonthlyTarget(targetAmountMinor: Long, savingGoalMinor: Long) {
        val uid = activeUserId.value ?: return
        val ym = selectedYearMonth.value
        viewModelScope.launch {
            val target = MonthlyTarget(
                id = "$uid-${ym.year}-${ym.monthValue}",
                userId = uid,
                month = ym.monthValue,
                year = ym.year,
                targetAmountMinor = targetAmountMinor,
                savingGoalMinor = savingGoalMinor,
                updatedAt = Instant.now(),
                syncStatus = SyncState.PENDING
            )
            targetRepository.saveMonthlyTarget(target)
            showBudgetDialogState.value = false
        }
    }

    fun clearUserMessage() {
        userMessageState.value = null
    }

    fun navigatePreviousMonth() {
        selectedYearMonth.update { it.minusMonths(1) }
    }

    fun navigateNextMonth() {
        val currentYm = YearMonth.now()
        selectedYearMonth.update { ym ->
            val nextYm = ym.plusMonths(1)
            if (nextYm.isAfter(currentYm)) ym else nextYm
        }
    }

    fun resetToCurrentMonth() {
        selectedYearMonth.value = YearMonth.now()
    }

    private fun formatMonthTitle(ym: YearMonth, language: AppLanguage): String {
        return if (language == AppLanguage.BANGLA) {
            val bnMonth = getBanglaMonthName(ym.monthValue)
            val bnYear = NumeralConverter.toBanglaDigits(ym.year.toString())
            "$bnMonth $bnYear-এর হিসাব"
        } else {
            val month = ym.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
            "$month Ledger"
        }
    }

    private fun formatMonthLabel(ym: YearMonth, language: AppLanguage): String {
        return if (language == AppLanguage.BANGLA) {
            getBanglaMonthName(ym.monthValue)
        } else {
            ym.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()))
        }
    }

    private fun getBanglaMonthName(month: Int): String {
        return when (month) {
            1 -> "জানুয়ারি"
            2 -> "ফেব্রুয়ারি"
            3 -> "মার্চ"
            4 -> "এপ্রিল"
            5 -> "মে"
            6 -> "জুন"
            7 -> "জুলাই"
            8 -> "আগস্ট"
            9 -> "সেপ্টেম্বর"
            10 -> "অক্টোবর"
            11 -> "নভেম্বর"
            12 -> "ডিসেম্বর"
            else -> "মাস"
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
