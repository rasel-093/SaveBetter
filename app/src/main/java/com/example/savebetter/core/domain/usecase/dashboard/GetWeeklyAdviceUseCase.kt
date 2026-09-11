package com.example.savebetter.core.domain.usecase.dashboard

import com.example.savebetter.R
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.WeeklyAdvice
import com.example.savebetter.core.domain.model.WeeklyAdviceType
import com.example.savebetter.core.domain.model.WeeklySummary
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.i18n.CurrencyFormatter
import com.example.savebetter.core.i18n.NumeralConverter
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Deterministic rule-based advice engine for Screen 05 (Weekly Detail).
 *
 * Rules:
 * 1. Category >= 25% of weekly spend: Highlighting top spending category.
 * 2. Repeated small same-category transactions: >= 3 transactions below threshold in one category.
 * 3. Projected target overshoot: Daily spend pace will overshoot weekly target.
 *
 * Requirements:
 * - No Machine Learning.
 * - No external network/API call.
 * - All text fully localized.
 */
class GetWeeklyAdviceUseCase @Inject constructor() {

    operator fun invoke(
        weeklySummary: WeeklySummary,
        weeklyExpenses: List<Expense>,
        categories: List<Category>,
        categoryNameResolver: (Category) -> String = { it.customName ?: it.nameKey ?: "Category" },
        referenceDate: LocalDate = LocalDate.now(),
        language: AppLanguage = AppLanguage.ENGLISH
    ): List<WeeklyAdvice> {
        val adviceList = mutableListOf<WeeklyAdvice>()
        val totalSpent = weeklySummary.spentAmountMinor
        val target = weeklySummary.targetAmountMinor

        if (totalSpent <= 0L) {
            return adviceList
        }

        val categoryMap = categories.associateBy { it.id }

        // Category spending aggregation
        val categoryExpenses = weeklyExpenses
            .filter { !it.isDeleted }
            .groupBy { it.categoryId }

        val categoryTotals = categoryExpenses.mapValues { (_, exps) -> exps.sumOf { it.amountMinor } }

        // -------------------------------------------------------------
        // Rule 1: Category >= 25% of weekly spend
        // -------------------------------------------------------------
        val dominantCategoryEntry = categoryTotals.entries
            .filter { (_, spent) -> spent.toFloat() / totalSpent >= 0.25f }
            .maxByOrNull { it.value }

        if (dominantCategoryEntry != null) {
            val cat = categoryMap[dominantCategoryEntry.key]
            val catName = cat?.let(categoryNameResolver) ?: "Category"
            val percentageInt = ((dominantCategoryEntry.value.toFloat() / totalSpent) * 100).toInt()
            val percentageStr = if (language == AppLanguage.BANGLA) {
                NumeralConverter.toBanglaDigits("$percentageInt%")
            } else {
                "$percentageInt%"
            }
            val formattedSpent = CurrencyFormatter.formatMinor(dominantCategoryEntry.value, language)

            adviceList.add(
                WeeklyAdvice(
                    id = "dominant_${dominantCategoryEntry.key}",
                    type = WeeklyAdviceType.CATEGORY_DOMINANT,
                    titleResId = R.string.weekly_advice_dominant_category_title,
                    titleArgs = listOf(catName, percentageStr),
                    messageResId = R.string.weekly_advice_dominant_category_desc,
                    messageArgs = listOf(catName, formattedSpent),
                    isWarning = false
                )
            )
        }

        // -------------------------------------------------------------
        // Rule 2: Repeated small same-category transactions
        // (>= 3 small transactions in the same category)
        // -------------------------------------------------------------
        val smallThresholdMinor = minOf(50000L, maxOf(10000L, totalSpent / 10L))
        for ((catId, exps) in categoryExpenses) {
            val smallExps = exps.filter { it.amountMinor <= smallThresholdMinor }
            if (smallExps.size >= 3) {
                val cat = categoryMap[catId]
                val catName = cat?.let(categoryNameResolver) ?: "Category"
                val countStr = if (language == AppLanguage.BANGLA) {
                    NumeralConverter.toBanglaDigits(smallExps.size.toString())
                } else {
                    smallExps.size.toString()
                }

                adviceList.add(
                    WeeklyAdvice(
                        id = "frequent_small_$catId",
                        type = WeeklyAdviceType.FREQUENT_SMALL,
                        titleResId = R.string.weekly_advice_frequent_small_title,
                        titleArgs = listOf(catName),
                        messageResId = R.string.weekly_advice_frequent_small_desc,
                        messageArgs = listOf(countStr, catName),
                        isWarning = false
                    )
                )
                break
            }
        }

        // -------------------------------------------------------------
        // Rule 3: Projected target overshoot
        // -------------------------------------------------------------
        val weekStartDate = runCatching { LocalDate.parse(weeklySummary.weekStart) }
            .getOrDefault(referenceDate.minusDays(referenceDate.dayOfWeek.value.toLong() - 1))
        val daysElapsed = (ChronoUnit.DAYS.between(weekStartDate, referenceDate) + 1).coerceIn(1L, 7L)
        val daysRemaining = maxOf(0L, 7L - daysElapsed)

        if (target > 0L) {
            val dailyAverage = totalSpent / daysElapsed
            val projectedTotal = dailyAverage * 7L

            if ((projectedTotal > target || weeklySummary.isWarning) && daysRemaining > 0L) {
                val remainingAllowance = weeklySummary.remainingAmountMinor / daysRemaining
                val formattedAllowance = CurrencyFormatter.formatMinor(remainingAllowance, language)
                val formattedTarget = CurrencyFormatter.formatMinor(target, language)
                val daysRemainingStr = if (language == AppLanguage.BANGLA) {
                    NumeralConverter.toBanglaDigits(daysRemaining.toString())
                } else {
                    daysRemaining.toString()
                }

                adviceList.add(
                    WeeklyAdvice(
                        id = "projected_overshoot",
                        type = WeeklyAdviceType.PROJECTED_OVERSHOOT,
                        titleResId = R.string.weekly_advice_overshoot_title,
                        titleArgs = listOf(formattedAllowance),
                        messageResId = R.string.weekly_advice_overshoot_desc,
                        messageArgs = listOf(formattedTarget, daysRemainingStr),
                        isWarning = true
                    )
                )
            } else if (weeklySummary.percentage < 0.60f && daysElapsed >= 4) {
                adviceList.add(
                    WeeklyAdvice(
                        id = "on_track",
                        type = WeeklyAdviceType.ON_TRACK,
                        titleResId = R.string.weekly_advice_on_track_title,
                        titleArgs = emptyList(),
                        messageResId = R.string.weekly_advice_on_track_desc,
                        messageArgs = emptyList(),
                        isWarning = false
                    )
                )
            }
        }

        return adviceList
    }
}
