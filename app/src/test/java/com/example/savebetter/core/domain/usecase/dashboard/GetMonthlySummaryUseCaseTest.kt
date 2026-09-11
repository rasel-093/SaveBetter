package com.example.savebetter.core.domain.usecase.dashboard

import app.cash.turbine.test
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
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

class GetMonthlySummaryUseCaseTest {

    private val targetRepository: TargetRepository = mockk()
    private val expenseRepository: ExpenseRepository = mockk()
    private lateinit var useCase: GetMonthlySummaryUseCase

    // Reference date: Day 10 of September 2026 (September has 30 days)
    private val testDate = LocalDate.of(2026, 9, 10)

    private val monthlyTarget = MonthlyTarget(
        id = "mt_1",
        userId = "user_1",
        month = 9,
        year = 2026,
        targetAmountMinor = 4000000L, // 40,000 BDT
        savingGoalMinor = 1000000L    // 10,000 BDT
    )

    private val salaryRecord = SalaryHandRecord(
        id = "sr_1",
        userId = "user_1",
        month = 9,
        year = 2026,
        salaryAmountMinor = 5000000L, // 50,000 BDT
        handRemainingAmountMinor = 5000000L
    )

    private fun instantOf(dateStr: String) =
        LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault()).toInstant()

    @Before
    fun setUp() {
        useCase = GetMonthlySummaryUseCase(targetRepository, expenseRepository)
    }

    @Test
    fun `monthly summary correctly computes spent, cash in hand, and projected month end pace`() = runTest {
        val expenses = listOf(
            Expense(
                id = "exp_1",
                userId = "user_1",
                amountMinor = 1000000L, // 10,000 BDT spent by day 10
                categoryId = "cat_1",
                date = instantOf("2026-09-05")
            ),
            // Expense from another month should be ignored
            Expense(
                id = "exp_2",
                userId = "user_1",
                amountMinor = 200000L,
                categoryId = "cat_1",
                date = instantOf("2026-08-15")
            )
        )

        every { targetRepository.observeMonthlyTargets("user_1") } returns flowOf(listOf(monthlyTarget))
        every { targetRepository.observeSalaryHandRecords("user_1") } returns flowOf(listOf(salaryRecord))
        every { expenseRepository.observeExpenses("user_1") } returns flowOf(expenses)

        useCase("user_1", testDate).test {
            val summary = awaitItem()
            assertEquals(4000000L, summary.targetAmountMinor)
            assertEquals(1000000L, summary.savingGoalMinor)
            assertEquals(1000000L, summary.spentAmountMinor)
            assertEquals(3000000L, summary.remainingAmountMinor)
            assertEquals(5000000L, summary.salaryAmountMinor)
            assertEquals(4000000L, summary.handRemainingMinor) // 50,000 - 10,000 = 40,000

            // Day 10 of 30 days: daily average = 1,000,000 / 10 = 100,000; projected = 100,000 * 30 = 3,000,000
            assertEquals(3000000L, summary.projectedSpentMinor)

            assertEquals(0.25f, summary.percentage, 0.001f)
            assertFalse(summary.isWarning)
            assertFalse(summary.isOverBudget)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `monthly summary triggers warning when spending reaches 80 percent`() = runTest {
        val expenses = listOf(
            Expense(
                id = "exp_1",
                userId = "user_1",
                amountMinor = 3400000L, // 34,000 of 40,000 = 85%
                categoryId = "cat_1",
                date = instantOf("2026-09-10")
            )
        )

        every { targetRepository.observeMonthlyTargets("user_1") } returns flowOf(listOf(monthlyTarget))
        every { targetRepository.observeSalaryHandRecords("user_1") } returns flowOf(listOf(salaryRecord))
        every { expenseRepository.observeExpenses("user_1") } returns flowOf(expenses)

        useCase("user_1", testDate).test {
            val summary = awaitItem()
            assertEquals(3400000L, summary.spentAmountMinor)
            assertEquals(0.85f, summary.percentage, 0.001f)
            assertTrue(summary.isWarning)
            assertFalse(summary.isOverBudget)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
