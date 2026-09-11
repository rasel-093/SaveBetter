package com.example.savebetter.core.domain.usecase.dashboard

import app.cash.turbine.test
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.WeeklyTarget
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GetWeeklySummaryUseCaseTest {

    private val targetRepository: TargetRepository = mockk()
    private val expenseRepository: ExpenseRepository = mockk()
    private lateinit var useCase: GetWeeklySummaryUseCase

    // Reference date: Wednesday 2026-09-09
    // Week start (Monday): 2026-09-07
    // Week end (Sunday): 2026-09-13
    private val testDate = LocalDate.of(2026, 9, 9)

    private val weeklyTarget = WeeklyTarget(
        id = "wt_1",
        userId = "user_1",
        weekStart = "2026-09-07",
        weekEnd = "2026-09-13",
        targetAmountMinor = 1000000L // 10,000 BDT
    )

    private fun instantOf(dateStr: String) =
        LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault()).toInstant()

    @Before
    fun setUp() {
        useCase = GetWeeklySummaryUseCase(targetRepository, expenseRepository)
    }

    @Test
    fun `normal spending under 80 percent emits isWarning false and isOverBudget false`() = runTest {
        val expenses = listOf(
            Expense(
                id = "exp_1",
                userId = "user_1",
                amountMinor = 500000L, // 5,000 BDT (50%)
                categoryId = "cat_1",
                date = instantOf("2026-09-08")
            )
        )

        every { targetRepository.observeWeeklyTargets("user_1") } returns flowOf(listOf(weeklyTarget))
        every { expenseRepository.observeExpenses("user_1") } returns flowOf(expenses)

        useCase("user_1", testDate).test {
            val summary = awaitItem()
            assertEquals(1000000L, summary.targetAmountMinor)
            assertEquals(500000L, summary.spentAmountMinor)
            assertEquals(500000L, summary.remainingAmountMinor)
            assertEquals(0.5f, summary.percentage, 0.001f)
            assertFalse(summary.isWarning)
            assertFalse(summary.isOverBudget)
            assertEquals(1000000L / 7L, summary.dailyLimitMinor)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `spending between 80 and 99 percent triggers isWarning true and isOverBudget false`() = runTest {
        val expenses = listOf(
            Expense(
                id = "exp_1",
                userId = "user_1",
                amountMinor = 850000L, // 8,500 BDT (85%)
                categoryId = "cat_1",
                date = instantOf("2026-09-09")
            )
        )

        every { targetRepository.observeWeeklyTargets("user_1") } returns flowOf(listOf(weeklyTarget))
        every { expenseRepository.observeExpenses("user_1") } returns flowOf(expenses)

        useCase("user_1", testDate).test {
            val summary = awaitItem()
            assertEquals(850000L, summary.spentAmountMinor)
            assertEquals(150000L, summary.remainingAmountMinor)
            assertEquals(0.85f, summary.percentage, 0.001f)
            assertTrue(summary.isWarning)
            assertFalse(summary.isOverBudget)
            assertEquals(850000L, summary.dailySpentMinor) // matches testDate 2026-09-09
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `spending at or above 100 percent triggers both isWarning and isOverBudget true`() = runTest {
        val expenses = listOf(
            Expense(
                id = "exp_1",
                userId = "user_1",
                amountMinor = 1100000L, // 11,000 BDT (110%)
                categoryId = "cat_1",
                date = instantOf("2026-09-08")
            )
        )

        every { targetRepository.observeWeeklyTargets("user_1") } returns flowOf(listOf(weeklyTarget))
        every { expenseRepository.observeExpenses("user_1") } returns flowOf(expenses)

        useCase("user_1", testDate).test {
            val summary = awaitItem()
            assertEquals(1100000L, summary.spentAmountMinor)
            assertEquals(0L, summary.remainingAmountMinor) // remaining does not go below 0
            assertEquals(1.10f, summary.percentage, 0.001f)
            assertTrue(summary.isWarning)
            assertTrue(summary.isOverBudget)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
