package com.example.savebetter.core.domain.usecase.dashboard

import com.example.savebetter.R
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlySuggestionType
import com.example.savebetter.core.domain.model.MonthlySummary
import com.example.savebetter.core.i18n.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GetMonthlyReductionSuggestionsUseCaseTest {

    private lateinit var useCase: GetMonthlyReductionSuggestionsUseCase

    private val categories = listOf(
        Category(
            id = "cat_health",
            userId = "user_1",
            nameKey = "category_health",
            customName = "Health",
            icon = "health",
            colorToken = "cat2",
            isDefault = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        ),
        Category(
            id = "cat_grocery",
            userId = "user_1",
            nameKey = "category_grocery",
            customName = "Market/Grocery",
            icon = "cart",
            colorToken = "cat5",
            isDefault = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        ),
        Category(
            id = "cat_household",
            userId = "user_1",
            nameKey = "category_household",
            customName = "Household",
            icon = "home",
            colorToken = "cat1",
            isDefault = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    )

    @Before
    fun setUp() {
        useCase = GetMonthlyReductionSuggestionsUseCase()
    }

    @Test
    fun `empty expenses returns empty suggestions list`() {
        val summary = MonthlySummary(
            targetAmountMinor = 4000000L,
            spentAmountMinor = 0L,
            remainingAmountMinor = 4000000L,
            savingGoalMinor = 1000000L,
            handRemainingMinor = 5000000L,
            projectedSpentMinor = 0L,
            percentage = 0f,
            isWarning = false,
            isOverBudget = false,
            year = 2026,
            month = 9
        )

        val result = useCase(
            currentMonthExpenses = emptyList(),
            previousMonthExpenses = emptyList(),
            categories = categories,
            monthlySummary = summary
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `single large one-time expense is excluded as outlier and does not trigger reduction suggestion`() {
        // Mockup Screen 06 scenario:
        // Household has a ৳15,000 (1,500,000 minor units) single one-time transaction
        // Plus regular grocery and health expenses
        val currentExpenses = listOf(
            Expense(
                id = "e_outlier",
                userId = "user_1",
                amountMinor = 1500000L, // ৳15,000 single large transaction
                categoryId = "cat_household",
                date = Instant.parse("2026-09-05T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            ),
            Expense(
                id = "e_health_1",
                userId = "user_1",
                amountMinor = 300000L,
                categoryId = "cat_health",
                date = Instant.parse("2026-09-08T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            ),
            Expense(
                id = "e_health_2",
                userId = "user_1",
                amountMinor = 400000L,
                categoryId = "cat_health",
                date = Instant.parse("2026-09-12T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )

        // Previous month had 0 in Health and 0 in Household
        val prevExpenses = listOf(
            Expense(
                id = "prev_1",
                userId = "user_1",
                amountMinor = 200000L,
                categoryId = "cat_health",
                date = Instant.parse("2026-08-10T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )

        val summary = MonthlySummary(
            targetAmountMinor = 4000000L,
            spentAmountMinor = 2200000L,
            remainingAmountMinor = 1800000L,
            savingGoalMinor = 1000000L,
            handRemainingMinor = 2800000L,
            projectedSpentMinor = 3500000L,
            percentage = 0.55f,
            isWarning = false,
            isOverBudget = false,
            year = 2026,
            month = 9
        )

        val result = useCase(
            currentMonthExpenses = currentExpenses,
            previousMonthExpenses = prevExpenses,
            categories = categories,
            monthlySummary = summary
        )

        // 1. Verify outlier is identified and explained
        val outlierSuggestion = result.find { it.type == MonthlySuggestionType.OUTLIER_EXCLUDED }
        assertTrue(outlierSuggestion != null)
        assertEquals(R.string.monthly_outlier_excluded_title, outlierSuggestion!!.titleResId)
        assertEquals("Household", outlierSuggestion.titleArgs[0])
        assertTrue(outlierSuggestion.titleArgs[1].contains("৳"))

        // 2. Verify Household is NOT recommended for reduction
        val householdReduction = result.find {
            it.type == MonthlySuggestionType.REDUCTION_SUGGESTION && it.titleArgs.contains("Household")
        }
        assertTrue(householdReduction == null)

        // 3. Verify Health IS recommended for reduction because recurring spend increased from ৳2,000 to ৳7,000
        val healthReduction = result.find {
            it.type == MonthlySuggestionType.REDUCTION_SUGGESTION && it.titleArgs.contains("Health")
        }
        assertTrue(healthReduction != null)
        assertEquals(R.string.monthly_reduction_suggestion_title, healthReduction!!.titleResId)
        assertTrue(healthReduction.isWarning)
    }

    @Test
    fun `savings goal feasibility is suggested when current spend is within target`() {
        val currentExpenses = listOf(
            Expense(
                id = "e1",
                userId = "user_1",
                amountMinor = 100000L,
                categoryId = "cat_grocery",
                date = Instant.parse("2026-09-05T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )

        val summary = MonthlySummary(
            targetAmountMinor = 4000000L,
            spentAmountMinor = 100000L,
            remainingAmountMinor = 3900000L,
            savingGoalMinor = 1000000L,
            handRemainingMinor = 4900000L,
            projectedSpentMinor = 500000L,
            percentage = 0.025f,
            isWarning = false,
            isOverBudget = false,
            year = 2026,
            month = 9
        )

        val result = useCase(
            currentMonthExpenses = currentExpenses,
            previousMonthExpenses = emptyList(),
            categories = categories,
            monthlySummary = summary
        )

        val savingsSuggestion = result.find { it.type == MonthlySuggestionType.SAVINGS_FEASIBLE }
        assertTrue(savingsSuggestion != null)
        assertFalse(savingsSuggestion!!.isWarning)
        assertTrue(savingsSuggestion.messageArgs[0].contains("৳"))
    }

    @Test
    fun `generates Bengali formatted numerals when language is Bangla`() {
        val currentExpenses = listOf(
            Expense(
                id = "e1",
                userId = "user_1",
                amountMinor = 500000L,
                categoryId = "cat_health",
                date = Instant.parse("2026-09-05T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )
        val prevExpenses = listOf(
            Expense(
                id = "p1",
                userId = "user_1",
                amountMinor = 200000L,
                categoryId = "cat_health",
                date = Instant.parse("2026-08-05T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )

        val summary = MonthlySummary(
            targetAmountMinor = 4000000L,
            spentAmountMinor = 500000L,
            remainingAmountMinor = 3500000L,
            savingGoalMinor = 1000000L,
            handRemainingMinor = 4500000L,
            projectedSpentMinor = 1500000L,
            percentage = 0.125f,
            isWarning = false,
            isOverBudget = false,
            year = 2026,
            month = 9
        )

        val result = useCase(
            currentMonthExpenses = currentExpenses,
            previousMonthExpenses = prevExpenses,
            categories = categories,
            monthlySummary = summary,
            language = AppLanguage.BANGLA
        )

        val healthReduction = result.find { it.type == MonthlySuggestionType.REDUCTION_SUGGESTION }
        assertTrue(healthReduction != null)
        // 100% in Bangla: ১০০%
        assertEquals("১০০%", healthReduction!!.messageArgs[2])
    }
}
