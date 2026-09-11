package com.example.savebetter.feature.home

import app.cash.turbine.test
import com.example.savebetter.core.designsystem.component.SyncStatus
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlySummary
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklySummary
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.usecase.dashboard.GetMonthlySummaryUseCase
import com.example.savebetter.core.domain.usecase.dashboard.GetWeeklySummaryUseCase
import com.example.savebetter.core.domain.usecase.profile.GetUserProfileUseCase
import com.example.savebetter.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getUserProfileUseCase: GetUserProfileUseCase = mockk()
    private val getWeeklySummaryUseCase: GetWeeklySummaryUseCase = mockk()
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase = mockk()
    private val expenseRepository: ExpenseRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()

    private lateinit var viewModel: HomeViewModel

    private val testProfile = UserProfile(
        id = "user_123",
        name = "Rasel",
        email = "rasel@example.com",
        monthlySalaryMinor = 6000000L,
        onboardingCompleted = true,
        syncStatus = SyncState.SYNCED
    )

    private val testWeekly = WeeklySummary(
        targetAmountMinor = 1000000L,
        spentAmountMinor = 400000L,
        remainingAmountMinor = 600000L,
        percentage = 0.4f
    )

    private val testMonthly = MonthlySummary(
        targetAmountMinor = 4000000L,
        spentAmountMinor = 1500000L,
        remainingAmountMinor = 2500000L
    )

    private val testCategory = Category(
        id = "cat_grocery",
        userId = "user_123",
        nameKey = "category_grocery",
        customName = "Grocery",
        icon = "shopping_cart",
        colorToken = "cat1",
        isDefault = true
    )

    private val testExpenses = listOf(
        Expense(
            id = "exp_1",
            userId = "user_123",
            amountMinor = 120000L,
            categoryId = "cat_grocery",
            note = "Weekly groceries",
            date = LocalDate.parse("2026-09-10").atStartOfDay(ZoneId.systemDefault()).toInstant()
        )
    )

    @Before
    fun setUp() {
        every { getUserProfileUseCase("user_123") } returns flowOf(testProfile)
        every { getWeeklySummaryUseCase("user_123") } returns flowOf(testWeekly)
        every { getMonthlySummaryUseCase("user_123") } returns flowOf(testMonthly)
        every { expenseRepository.observeExpenses("user_123") } returns flowOf(testExpenses)
        every { categoryRepository.observeCategories("user_123") } returns flowOf(listOf(testCategory))

        viewModel = HomeViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            getWeeklySummaryUseCase = getWeeklySummaryUseCase,
            getMonthlySummaryUseCase = getMonthlySummaryUseCase,
            expenseRepository = expenseRepository,
            categoryRepository = categoryRepository
        )
    }

    @Test
    fun `uiState combines profile, summaries, and maps recent expenses with categories`() = runTest {
        viewModel.initForUser("user_123")

        viewModel.uiState.test {
            val state = awaitItem()
            val finalState = if (state.isLoading) awaitItem() else state

            assertFalse(finalState.isLoading)
            assertNotNull(finalState.userProfile)
            assertEquals("Rasel", finalState.userProfile?.name)
            assertEquals(1000000L, finalState.weeklySummary.targetAmountMinor)
            assertEquals(4000000L, finalState.monthlySummary.targetAmountMinor)
            assertEquals(SyncStatus.Synced, finalState.syncStatus)

            assertEquals(1, finalState.recentExpenses.size)
            val firstExpense = finalState.recentExpenses.first()
            assertEquals("Weekly groceries", firstExpense.title)
            assertEquals(120000L, firstExpense.amountMinor)
            assertEquals("Grocery", firstExpense.categoryName)
        }
    }
}
