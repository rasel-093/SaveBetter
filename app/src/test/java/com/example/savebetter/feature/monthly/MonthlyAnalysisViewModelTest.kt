package com.example.savebetter.feature.monthly

import app.cash.turbine.test
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlySuggestion
import com.example.savebetter.core.domain.model.MonthlySuggestionType
import com.example.savebetter.core.domain.model.MonthlySummary
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import com.example.savebetter.core.domain.usecase.dashboard.GetMonthlyReductionSuggestionsUseCase
import com.example.savebetter.core.domain.usecase.dashboard.GetMonthlySummaryUseCase
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
import java.time.YearMonth

class MonthlyAnalysisViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase = mockk()
    private val getMonthlyReductionSuggestionsUseCase: GetMonthlyReductionSuggestionsUseCase = mockk()
    private val expenseRepository: ExpenseRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val targetRepository: TargetRepository = mockk(relaxed = true)

    private lateinit var viewModel: MonthlyAnalysisViewModel

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
        ),
        Category(
            id = "cat_2",
            userId = "user_1",
            nameKey = "category_health",
            customName = "Health",
            icon = "health",
            colorToken = "cat2",
            isDefault = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    )

    private val nowYear = YearMonth.now().year
    private val nowMonth = YearMonth.now().monthValue

    private val testSummary = MonthlySummary(
        targetAmountMinor = 4000000L,
        spentAmountMinor = 2500000L,
        remainingAmountMinor = 1500000L,
        savingGoalMinor = 1000000L,
        handRemainingMinor = 2500000L,
        projectedSpentMinor = 3000000L,
        percentage = 0.625f,
        isWarning = false,
        isOverBudget = false,
        year = nowYear,
        month = nowMonth
    )

    private val testExpenses = listOf(
        Expense(
            id = "exp_1",
            userId = "user_1",
            amountMinor = 1500000L, // ৳15,000 Household
            categoryId = "cat_1",
            date = Instant.now(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        ),
        Expense(
            id = "exp_2",
            userId = "user_1",
            amountMinor = 1000000L, // ৳10,000 Health
            categoryId = "cat_2",
            date = Instant.now(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    )

    @Before
    fun setUp() {
        every { getMonthlySummaryUseCase("user_1", any()) } returns flowOf(testSummary)
        coEvery { expenseRepository.observeExpenses("user_1") } returns flowOf(testExpenses)
        coEvery { categoryRepository.observeCategories("user_1") } returns flowOf(testCategories)
        every {
            getMonthlyReductionSuggestionsUseCase(any(), any(), any(), any(), any(), any())
        } returns listOf(
            MonthlySuggestion(
                id = "sugg_1",
                type = MonthlySuggestionType.SAVINGS_FEASIBLE,
                titleResId = 1,
                titleArgs = emptyList(),
                messageResId = 2,
                messageArgs = listOf("৳10,000"),
                isWarning = false
            )
        )

        viewModel = MonthlyAnalysisViewModel(
            getMonthlySummaryUseCase = getMonthlySummaryUseCase,
            getMonthlyReductionSuggestionsUseCase = getMonthlyReductionSuggestionsUseCase,
            expenseRepository = expenseRepository,
            categoryRepository = categoryRepository,
            targetRepository = targetRepository
        )
    }

    @Test
    fun `initForUser populates monthly summary, category slices, and comparison items`() = runTest {
        viewModel.initForUser("user_1")

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(4000000L, state.targetAmountMinor)
            assertEquals(2500000L, state.spentAmountMinor)

            // Category slices (sorted descending)
            assertEquals(2, state.categorySlices.size)
            assertEquals("Household", state.categorySlices[0].label)
            assertEquals(15000f, state.categorySlices[0].value, 0.01f)
            assertEquals("Health", state.categorySlices[1].label)
            assertEquals(10000f, state.categorySlices[1].value, 0.01f)

            // Comparison bars
            assertNotNull(state.comparisonCurrent)
            assertNotNull(state.comparisonPrevious)
            assertEquals(25000f, state.comparisonCurrent!!.value, 0.01f)

            // Suggestions
            assertEquals(1, state.suggestions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigatePreviousMonth and navigateNextMonth updates selected YearMonth`() = runTest {
        viewModel.initForUser("user_1")

        viewModel.uiState.test {
            val initial = awaitItem()
            val initialYm = initial.selectedYearMonth

            viewModel.navigatePreviousMonth()
            val prev = awaitItem()
            assertEquals(initialYm.minusMonths(1), prev.selectedYearMonth)

            viewModel.navigateNextMonth()
            val next = awaitItem()
            assertEquals(initialYm, next.selectedYearMonth)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setLanguage updates month title to Bangla`() = runTest {
        viewModel.initForUser("user_1", AppLanguage.ENGLISH)

        viewModel.uiState.test {
            val en = awaitItem()
            assertTrue(en.monthTitleText.contains("Ledger"))

            viewModel.setLanguage(AppLanguage.BANGLA)
            val bn = awaitItem()
            assertTrue(bn.monthTitleText.contains("হিসাব"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showBudgetDialog updates dialog state and saveMonthlyTarget calls repository`() = runTest {
        viewModel.initForUser("user_1")

        viewModel.uiState.test {
            var state = awaitItem()
            assertFalse(state.showBudgetDialog)

            viewModel.showBudgetDialog(true)
            state = awaitItem()
            assertTrue(state.showBudgetDialog)

            viewModel.saveMonthlyTarget(4500000L, 1200000L)
            state = awaitItem()
            assertFalse(state.showBudgetDialog)

            io.mockk.coVerify {
                targetRepository.saveMonthlyTarget(
                    match {
                        it.userId == "user_1" &&
                        it.targetAmountMinor == 4500000L &&
                        it.savingGoalMinor == 1200000L
                    }
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
