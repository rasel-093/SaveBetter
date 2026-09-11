package com.example.savebetter.core.domain.usecase.dashboard

import com.example.savebetter.R
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.WeeklyAdviceType
import com.example.savebetter.core.domain.model.WeeklySummary
import com.example.savebetter.core.i18n.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class GetWeeklyAdviceUseCaseTest {

    private lateinit var useCase: GetWeeklyAdviceUseCase

    private val categories = listOf(
        Category(
            id = "cat_transport",
            userId = "user_1",
            nameKey = "category_transport",
            customName = "Transport",
            icon = "bus",
            colorToken = "cat4",
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
        useCase = GetWeeklyAdviceUseCase()
    }

    @Test
    fun `empty expenses returns empty advice list`() {
        val summary = WeeklySummary(
            targetAmountMinor = 1000000L,
            spentAmountMinor = 0L,
            remainingAmountMinor = 1000000L,
            percentage = 0f,
            dailyLimitMinor = 142857L,
            dailySpentMinor = 0L,
            isWarning = false,
            isOverBudget = false,
            weekStart = "2026-09-07",
            weekEnd = "2026-09-13"
        )

        val result = useCase(
            weeklySummary = summary,
            weeklyExpenses = emptyList(),
            categories = categories,
            referenceDate = LocalDate.parse("2026-09-10")
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `Rule 1 triggers when category is greater than or equal to 25 percent of weekly spend`() {
        val expenses = listOf(
            Expense(
                id = "e1",
                userId = "user_1",
                amountMinor = 400000L, // ৳4,000 in Transport (40% of 10,000)
                categoryId = "cat_transport",
                date = Instant.parse("2026-09-08T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            ),
            Expense(
                id = "e2",
                userId = "user_1",
                amountMinor = 600000L, // ৳6,000 in Household (60% of 10,000)
                categoryId = "cat_household",
                date = Instant.parse("2026-09-09T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )

        val summary = WeeklySummary(
            targetAmountMinor = 1500000L,
            spentAmountMinor = 1000000L,
            remainingAmountMinor = 500000L,
            percentage = 0.67f,
            dailyLimitMinor = 214285L,
            dailySpentMinor = 0L,
            isWarning = false,
            isOverBudget = false,
            weekStart = "2026-09-07",
            weekEnd = "2026-09-13"
        )

        val result = useCase(
            weeklySummary = summary,
            weeklyExpenses = expenses,
            categories = categories,
            referenceDate = LocalDate.parse("2026-09-09")
        )

        val dominantAdvice = result.find { it.type == WeeklyAdviceType.CATEGORY_DOMINANT }
        assertTrue(dominantAdvice != null)
        assertEquals(R.string.weekly_advice_dominant_category_title, dominantAdvice!!.titleResId)
        // Top category is Household with 60%
        assertEquals("Household", dominantAdvice.titleArgs[0])
        assertEquals("60%", dominantAdvice.titleArgs[1])
    }

    @Test
    fun `Rule 2 triggers when at least 3 frequent small transactions exist in same category`() {
        val expenses = listOf(
            Expense(id = "e1", userId = "user_1", amountMinor = 15000L, categoryId = "cat_grocery", date = Instant.parse("2026-09-07T10:00:00Z"), createdAt = Instant.now(), updatedAt = Instant.now()),
            Expense(id = "e2", userId = "user_1", amountMinor = 20000L, categoryId = "cat_grocery", date = Instant.parse("2026-09-08T10:00:00Z"), createdAt = Instant.now(), updatedAt = Instant.now()),
            Expense(id = "e3", userId = "user_1", amountMinor = 10000L, categoryId = "cat_grocery", date = Instant.parse("2026-09-09T10:00:00Z"), createdAt = Instant.now(), updatedAt = Instant.now()),
            Expense(id = "e4", userId = "user_1", amountMinor = 500000L, categoryId = "cat_household", date = Instant.parse("2026-09-09T12:00:00Z"), createdAt = Instant.now(), updatedAt = Instant.now())
        )

        val summary = WeeklySummary(
            targetAmountMinor = 1000000L,
            spentAmountMinor = 545000L,
            remainingAmountMinor = 455000L,
            percentage = 0.545f,
            dailyLimitMinor = 142857L,
            dailySpentMinor = 0L,
            isWarning = false,
            isOverBudget = false,
            weekStart = "2026-09-07",
            weekEnd = "2026-09-13"
        )

        val result = useCase(
            weeklySummary = summary,
            weeklyExpenses = expenses,
            categories = categories,
            referenceDate = LocalDate.parse("2026-09-09")
        )

        val smallAdvice = result.find { it.type == WeeklyAdviceType.FREQUENT_SMALL }
        assertTrue(smallAdvice != null)
        assertEquals(R.string.weekly_advice_frequent_small_title, smallAdvice!!.titleResId)
        assertEquals("Market/Grocery", smallAdvice.titleArgs[0])
        assertEquals("3", smallAdvice.messageArgs[0])
    }

    @Test
    fun `Rule 3 triggers when projected spend exceeds target`() {
        // Spent ৳8,700 out of ৳10,000 in first 4 days -> daily avg ৳2,175 * 7 = ৳15,225 > ৳10,000
        val summary = WeeklySummary(
            targetAmountMinor = 1000000L,
            spentAmountMinor = 870000L,
            remainingAmountMinor = 130000L,
            percentage = 0.87f,
            dailyLimitMinor = 142857L,
            dailySpentMinor = 0L,
            isWarning = true,
            isOverBudget = false,
            weekStart = "2026-09-07",
            weekEnd = "2026-09-13"
        )

        val expenses = listOf(
            Expense(id = "e1", userId = "user_1", amountMinor = 870000L, categoryId = "cat_household", date = Instant.parse("2026-09-10T10:00:00Z"), createdAt = Instant.now(), updatedAt = Instant.now())
        )

        val result = useCase(
            weeklySummary = summary,
            weeklyExpenses = expenses,
            categories = categories,
            referenceDate = LocalDate.parse("2026-09-10") // Thursday = Day 4
        )

        val overshootAdvice = result.find { it.type == WeeklyAdviceType.PROJECTED_OVERSHOOT }
        assertTrue(overshootAdvice != null)
        assertTrue(overshootAdvice!!.isWarning)
        assertEquals(R.string.weekly_advice_overshoot_title, overshootAdvice.titleResId)
        // 3 days remaining, remaining = 130000 / 3 = 43333 minor units (৳433.33)
        assertTrue(overshootAdvice.titleArgs[0].contains("৳"))
    }

    @Test
    fun `generates localized Bangla digits when language is Bangla`() {
        val summary = WeeklySummary(
            targetAmountMinor = 1000000L,
            spentAmountMinor = 800000L,
            remainingAmountMinor = 200000L,
            percentage = 0.80f,
            dailyLimitMinor = 142857L,
            dailySpentMinor = 0L,
            isWarning = true,
            isOverBudget = false,
            weekStart = "2026-09-07",
            weekEnd = "2026-09-13"
        )

        val expenses = listOf(
            Expense(id = "e1", userId = "user_1", amountMinor = 800000L, categoryId = "cat_transport", date = Instant.parse("2026-09-08T10:00:00Z"), createdAt = Instant.now(), updatedAt = Instant.now())
        )

        val result = useCase(
            weeklySummary = summary,
            weeklyExpenses = expenses,
            categories = categories,
            referenceDate = LocalDate.parse("2026-09-08"),
            language = AppLanguage.BANGLA
        )

        val dominantAdvice = result.find { it.type == WeeklyAdviceType.CATEGORY_DOMINANT }
        assertTrue(dominantAdvice != null)
        // 100% in Bangla digits: ১০০%
        assertEquals("১০০%", dominantAdvice!!.titleArgs[1])
        assertTrue(dominantAdvice.messageArgs[1].contains("৳"))
    }
}
