package com.example.savebetter.feature.weekly

import app.cash.turbine.test
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.WeeklyAdvice
import com.example.savebetter.core.domain.model.WeeklyAdviceType
import com.example.savebetter.core.domain.model.WeeklySummary
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.usecase.dashboard.GetWeeklyAdviceUseCase
import com.example.savebetter.core.domain.usecase.dashboard.GetWeeklySummaryUseCase
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class WeeklyDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getWeeklySummaryUseCase: GetWeeklySummaryUseCase = mockk()
    private val getWeeklyAdviceUseCase: GetWeeklyAdviceUseCase = mockk()
    private val expenseRepository: ExpenseRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()

    private lateinit var viewModel: WeeklyDetailViewModel

    private val testCategories = listOf(
        Category(
            id = "cat_1",
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

    private val testSummary = WeeklySummary(
        targetAmountMinor = 1000000L,
        spentAmountMinor = 600000L,
        remainingAmountMinor = 400000L,
        percentage = 0.60f,
        dailyLimitMinor = 142857L,
        dailySpentMinor = 200000L,
        isWarning = false,
        isOverBudget = false,
        weekStart = "2026-09-07",
        weekEnd = "2026-09-13"
    )

    private val testExpenses = listOf(
        Expense(
            id = "exp_1",
            userId = "user_1",
            amountMinor = 200000L,
            categoryId = "cat_1",
            note = "Grocery store",
            date = Instant.parse("2026-09-08T10:00:00Z"),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        ),
        Expense(
            id = "exp_2",
            userId = "user_1",
            amountMinor = 400000L,
            categoryId = "cat_1",
            note = "Utensils",
            date = Instant.parse("2026-09-09T10:00:00Z"),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    )

    @Before
    fun setUp() {
        every { getWeeklySummaryUseCase("user_1", any()) } returns flowOf(testSummary)
        coEvery { expenseRepository.observeExpenses("user_1") } returns flowOf(testExpenses)
        coEvery { categoryRepository.observeCategories("user_1") } returns flowOf(testCategories)
        every {
            getWeeklyAdviceUseCase(any(), any(), any(), any(), any(), any())
        } returns listOf(
            WeeklyAdvice(
                id = "adv_1",
                type = WeeklyAdviceType.CATEGORY_DOMINANT,
                titleResId = 1,
                titleArgs = listOf("Household", "100%"),
                messageResId = 2,
                messageArgs = listOf("Household", "৳6,000"),
                isWarning = false
            )
        )

        viewModel = WeeklyDetailViewModel(
            getWeeklySummaryUseCase = getWeeklySummaryUseCase,
            getWeeklyAdviceUseCase = getWeeklyAdviceUseCase,
            expenseRepository = expenseRepository,
            categoryRepository = categoryRepository
        )
    }

    @Test
    fun `initForUser populates weekly summary, 7 daily trend bars, and advice`() = runTest {
        viewModel.initForUser("user_1")

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(1000000L, state.weeklySummary.targetAmountMinor)
            assertEquals(600000L, state.weeklySummary.spentAmountMinor)

            // 7 daily bars for Monday through Sunday
            assertEquals(7, state.dailyTrendBars.size)
            // Day with 400000L (exp_2 on Wednesday) should be peak and highlighted
            val highlightedBars = state.dailyTrendBars.filter { it.isHighlighted }
            assertEquals(1, highlightedBars.size)
            assertEquals(4000f, highlightedBars.first().amount, 0.01f)

            // Advice
            assertEquals(1, state.adviceList.size)
            assertEquals("Household", state.adviceList.first().titleArgs.first())

            // Expense items
            assertEquals(2, state.weeklyExpenses.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigatePreviousWeek and navigateNextWeek adjust date range`() = runTest {
        viewModel.initForUser("user_1")

        viewModel.uiState.test {
            val initial = awaitItem()
            val initialRef = initial.referenceDate

            viewModel.navigatePreviousWeek()
            val prev = awaitItem()
            assertEquals(initialRef.minusWeeks(1), prev.referenceDate)

            viewModel.navigateNextWeek()
            val next = awaitItem()
            assertEquals(initialRef, next.referenceDate)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setLanguage updates language in ui state`() = runTest {
        viewModel.initForUser("user_1", AppLanguage.ENGLISH)

        viewModel.uiState.test {
            val enState = awaitItem()
            assertTrue(enState.dailyTrendBars.any { it.label == "Mon" })

            viewModel.setLanguage(AppLanguage.BANGLA)
            val bnState = awaitItem()
            assertTrue(bnState.dailyTrendBars.any { it.label == "সোম" })

            cancelAndIgnoreRemainingEvents()
        }
    }
}
