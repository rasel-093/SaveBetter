package com.example.savebetter.feature.weekly

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.component.DailyBarData
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.usecase.dashboard.GetWeeklyAdviceUseCase
import com.example.savebetter.core.domain.usecase.dashboard.GetWeeklySummaryUseCase
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.i18n.NumeralConverter
import com.example.savebetter.feature.home.ExpenseItemUiModel
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeeklyDetailViewModel @Inject constructor(
    private val getWeeklySummaryUseCase: GetWeeklySummaryUseCase,
    private val getWeeklyAdviceUseCase: GetWeeklyAdviceUseCase,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val activeUserId = MutableStateFlow<String?>(null)
    private val referenceDate = MutableStateFlow(LocalDate.now())
    private val currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)

    val uiState: StateFlow<WeeklyDetailUiState> = combine(
        activeUserId,
        referenceDate,
        currentLanguage
    ) { userId, refDate, language ->
        Triple(userId, refDate, language)
    }.flatMapLatest { (userId, refDate, language) ->
        if (userId == null) {
            flowOf(WeeklyDetailUiState(isLoading = false))
        } else {
            val weekStart = refDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weekEnd = refDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

            combine(
                getWeeklySummaryUseCase(userId, refDate),
                expenseRepository.observeExpenses(userId),
                categoryRepository.observeCategories(userId)
            ) { weeklySummary, allExpenses, categories ->
                val activeCategories = categories.filter { !it.isDeleted }
                val categoryMap = activeCategories.associateBy { it.id }

                // Filter expenses strictly to this week
                val weeklyExpenses = allExpenses.filter { expense ->
                    if (expense.isDeleted) return@filter false
                    val date = expense.date.atZone(ZoneId.systemDefault()).toLocalDate()
                    !date.isBefore(weekStart) && !date.isAfter(weekEnd)
                }

                // 1. Build 7 daily trend bars
                val dailyTotals = mutableMapOf<LocalDate, Long>()
                for (i in 0L..6L) {
                    dailyTotals[weekStart.plusDays(i)] = 0L
                }
                weeklyExpenses.forEach { exp ->
                    val d = exp.date.atZone(ZoneId.systemDefault()).toLocalDate()
                    if (dailyTotals.containsKey(d)) {
                        dailyTotals[d] = (dailyTotals[d] ?: 0L) + exp.amountMinor
                    }
                }

                val maxDailyAmount = dailyTotals.values.maxOrNull() ?: 0L
                val dailyTrendBars = dailyTotals.map { (date, amountMinor) ->
                    val dayLabel = getDayLabel(date.dayOfWeek, language)
                    val amountFloat = amountMinor / 100f
                    val isPeak = amountMinor > 0L && amountMinor == maxDailyAmount
                    DailyBarData(
                        label = dayLabel,
                        amount = amountFloat,
                        isHighlighted = isPeak
                    )
                }

                // 2. Build advice list
                val adviceList = getWeeklyAdviceUseCase(
                    weeklySummary = weeklySummary,
                    weeklyExpenses = weeklyExpenses,
                    categories = activeCategories,
                    referenceDate = refDate,
                    language = language
                )

                // 3. Format Date Range (e.g., "8 – 14 Sep" or "৮ – ১৪ সেপ্টে")
                val dateRangeStr = formatDateRange(weekStart, weekEnd, language)

                // 4. Map weekly expenses for list display
                val expenseItems = weeklyExpenses
                    .sortedByDescending { it.date }
                    .map { exp ->
                        val cat = categoryMap[exp.categoryId]
                        val catColor = getCategoryColor(cat?.colorToken)
                        val expLocalDate = exp.date.atZone(ZoneId.systemDefault()).toLocalDate()
                        val formattedDate = expLocalDate.format(DateTimeFormatter.ofPattern("dd MMM"))
                        val dateDisplay = if (language == AppLanguage.BANGLA) {
                            NumeralConverter.toBanglaDigits(formattedDate)
                        } else {
                            formattedDate
                        }

                        ExpenseItemUiModel(
                            id = exp.id,
                            title = exp.note?.ifBlank { null } ?: (cat?.customName ?: cat?.nameKey ?: "Expense"),
                            categoryName = cat?.customName ?: cat?.nameKey ?: "General",
                            categoryColor = catColor,
                            amountMinor = exp.amountMinor,
                            date = dateDisplay,
                            note = exp.note
                        )
                    }

                WeeklyDetailUiState(
                    weeklySummary = weeklySummary,
                    weekDateRangeText = dateRangeStr,
                    referenceDate = refDate,
                    dailyTrendBars = dailyTrendBars,
                    adviceList = adviceList,
                    weeklyExpenses = expenseItems,
                    categories = activeCategories,
                    isLoading = false
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeeklyDetailUiState(isLoading = true)
    )

    fun initForUser(userId: String, language: AppLanguage = AppLanguage.ENGLISH) {
        activeUserId.value = userId
        currentLanguage.value = language
    }

    fun setLanguage(language: AppLanguage) {
        currentLanguage.value = language
    }

    fun navigatePreviousWeek() {
        referenceDate.update { it.minusWeeks(1) }
    }

    fun navigateNextWeek() {
        referenceDate.update { it.plusWeeks(1) }
    }

    fun resetToCurrentWeek() {
        referenceDate.value = LocalDate.now()
    }

    private fun getDayLabel(dayOfWeek: DayOfWeek, language: AppLanguage): String {
        return if (language == AppLanguage.BANGLA) {
            when (dayOfWeek) {
                DayOfWeek.SATURDAY -> "শনি"
                DayOfWeek.SUNDAY -> "রবি"
                DayOfWeek.MONDAY -> "সোম"
                DayOfWeek.TUESDAY -> "মঙ্গল"
                DayOfWeek.WEDNESDAY -> "বুধ"
                DayOfWeek.THURSDAY -> "বৃহঃ"
                DayOfWeek.FRIDAY -> "শুক্র"
            }
        } else {
            when (dayOfWeek) {
                DayOfWeek.SATURDAY -> "Sat"
                DayOfWeek.SUNDAY -> "Sun"
                DayOfWeek.MONDAY -> "Mon"
                DayOfWeek.TUESDAY -> "Tue"
                DayOfWeek.WEDNESDAY -> "Wed"
                DayOfWeek.THURSDAY -> "Thu"
                DayOfWeek.FRIDAY -> "Fri"
            }
        }
    }

    private fun formatDateRange(start: LocalDate, end: LocalDate, language: AppLanguage): String {
        val startDay = start.dayOfMonth.toString()
        val endDay = end.dayOfMonth.toString()
        val month = end.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))

        return if (language == AppLanguage.BANGLA) {
            val bnStart = NumeralConverter.toBanglaDigits(startDay)
            val bnEnd = NumeralConverter.toBanglaDigits(endDay)
            val bnMonth = when (end.monthValue) {
                1 -> "জানু"
                2 -> "ফেব্রু"
                3 -> "মার্চ"
                4 -> "এপ্রিল"
                5 -> "মে"
                6 -> "জুন"
                7 -> "জুলাই"
                8 -> "আগস্ট"
                9 -> "সেপ্টে"
                10 -> "অক্টো"
                11 -> "নভে"
                12 -> "ডিসে"
                else -> month
            }
            "$bnStart – $bnEnd $bnMonth"
        } else {
            "$startDay – $endDay $month"
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
