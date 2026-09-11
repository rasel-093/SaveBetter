package com.example.savebetter.core.domain.usecase.dashboard

import com.example.savebetter.R
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlySuggestion
import com.example.savebetter.core.domain.model.MonthlySuggestionType
import com.example.savebetter.core.domain.model.MonthlySummary
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.i18n.CurrencyFormatter
import com.example.savebetter.core.i18n.NumeralConverter
import javax.inject.Inject

/**
 * Intelligent deterministic monthly expense suggestion engine.
 *
 * Rules:
 * 1. Outlier Exclusion: Detects single large one-time spikes (e.g. >= 50% of category spend and >= ৳3,000)
 *    and explicitly explains to the user that it was excluded from recurring trend analysis.
 *    CRITICAL: Never recommends reducing a category solely because of an outlier transaction.
 * 2. Month-over-Month Increase + Absolute Share: Suggests reduction only for recurring categories
 *    where recurring spend increased compared to last month AND accounts for >= 15% of total spend.
 * 3. Savings Goal Feasibility: Explains when savings goal is achievable based on current spending pace.
 */
class GetMonthlyReductionSuggestionsUseCase @Inject constructor() {

    operator fun invoke(
        currentMonthExpenses: List<Expense>,
        previousMonthExpenses: List<Expense>,
        categories: List<Category>,
        monthlySummary: MonthlySummary,
        categoryNameResolver: (Category) -> String = { it.customName ?: it.nameKey ?: "Category" },
        language: AppLanguage = AppLanguage.ENGLISH
    ): List<MonthlySuggestion> {
        val suggestions = mutableListOf<MonthlySuggestion>()

        val activeCurrentExpenses = currentMonthExpenses.filter { !it.isDeleted }
        val activePrevExpenses = previousMonthExpenses.filter { !it.isDeleted }

        if (activeCurrentExpenses.isEmpty()) {
            return suggestions
        }

        val categoryMap = categories.associateBy { it.id }

        // Group by category
        val currentByCategory = activeCurrentExpenses.groupBy { it.categoryId }
        val prevByCategory = activePrevExpenses.groupBy { it.categoryId }

        val totalCurrentSpend = activeCurrentExpenses.sumOf { it.amountMinor }

        // Map of recurring spending per category (excluding detected outliers)
        val recurringCategorySpend = mutableMapOf<String, Long>()
        val excludedOutlierCategories = mutableSetOf<String>()

        // 1. Outlier Detection
        for ((catId, expenses) in currentByCategory) {
            val catTotal = expenses.sumOf { it.amountMinor }
            val cat = categoryMap[catId]
            val catName = cat?.let(categoryNameResolver) ?: "Category"

            // An expense is an outlier if:
            // - It constitutes >= 50% of the category's total
            // - The category has multiple transactions (or this transaction is >= 300,000 minor units / ৳3,000)
            // - Its value is significantly above the average of the remaining transactions
            val maxExpense = expenses.maxByOrNull { it.amountMinor }
            if (maxExpense != null && maxExpense.amountMinor >= 300000L && catTotal > 0L) {
                val shareInCat = maxExpense.amountMinor.toFloat() / catTotal
                val isOutlier = (expenses.size == 1 && maxExpense.amountMinor >= 1000000L) ||
                        (expenses.size > 1 && shareInCat >= 0.50f)

                if (isOutlier) {
                    excludedOutlierCategories.add(catId)
                    val recurringSpend = catTotal - maxExpense.amountMinor
                    recurringCategorySpend[catId] = recurringSpend

                    val formattedOutlier = CurrencyFormatter.formatMinor(maxExpense.amountMinor, language)
                    suggestions.add(
                        MonthlySuggestion(
                            id = "outlier_$catId",
                            type = MonthlySuggestionType.OUTLIER_EXCLUDED,
                            titleResId = R.string.monthly_outlier_excluded_title,
                            titleArgs = listOf(catName, formattedOutlier),
                            messageResId = R.string.monthly_outlier_excluded_desc,
                            messageArgs = listOf(formattedOutlier),
                            isWarning = false
                        )
                    )
                    continue
                }
            }
            recurringCategorySpend[catId] = catTotal
        }

        val totalRecurringSpend = recurringCategorySpend.values.sum().coerceAtLeast(1L)

        // 2. Month-over-Month Increase + Absolute Share of Spending (excluding outliers)
        for ((catId, recurringSpend) in recurringCategorySpend) {
            // Do not advise reduction solely because of a category dominated by an outlier
            if (excludedOutlierCategories.contains(catId) && recurringSpend == 0L) {
                continue
            }

            val prevSpend = prevByCategory[catId]?.sumOf { it.amountMinor } ?: 0L
            val shareOfTotal = recurringSpend.toFloat() / totalRecurringSpend

            // Condition:
            // - Absolute share >= 15% of total recurring spend
            // - Spend increased compared to last month (by at least 10% or from 0 with multiple transactions)
            val increaseAmount = recurringSpend - prevSpend
            val hasMeaningfulIncrease = if (prevSpend > 0) {
                increaseAmount.toFloat() / prevSpend >= 0.10f
            } else {
                recurringSpend >= 100000L // ৳1,000+ new category
            }

            if (shareOfTotal >= 0.15f && hasMeaningfulIncrease) {
                val cat = categoryMap[catId]
                val catName = cat?.let(categoryNameResolver) ?: "Category"
                val sharePercentInt = (shareOfTotal * 100).toInt()
                val sharePercentStr = if (language == AppLanguage.BANGLA) {
                    NumeralConverter.toBanglaDigits("$sharePercentInt%")
                } else {
                    "$sharePercentInt%"
                }
                val formattedIncrease = CurrencyFormatter.formatMinor(increaseAmount.coerceAtLeast(0L), language)

                suggestions.add(
                    MonthlySuggestion(
                        id = "reduction_$catId",
                        type = MonthlySuggestionType.REDUCTION_SUGGESTION,
                        titleResId = R.string.monthly_reduction_suggestion_title,
                        titleArgs = listOf(catName),
                        messageResId = R.string.monthly_reduction_suggestion_desc,
                        messageArgs = listOf(catName, formattedIncrease, sharePercentStr),
                        isWarning = true
                    )
                )
            }
        }

        // 3. Savings Feasibility
        val target = monthlySummary.targetAmountMinor
        val savingGoal = monthlySummary.savingGoalMinor
        if (savingGoal > 0L && totalCurrentSpend <= target) {
            val formattedGoal = CurrencyFormatter.formatMinor(savingGoal, language)
            suggestions.add(
                MonthlySuggestion(
                    id = "savings_feasible",
                    type = MonthlySuggestionType.SAVINGS_FEASIBLE,
                    titleResId = R.string.monthly_savings_feasible_title,
                    titleArgs = emptyList(),
                    messageResId = R.string.monthly_savings_feasible_desc,
                    messageArgs = listOf(formattedGoal),
                    isWarning = false
                )
            )
        }

        return suggestions
    }
}
