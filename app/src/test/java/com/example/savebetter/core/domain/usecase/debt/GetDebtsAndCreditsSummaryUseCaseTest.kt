package com.example.savebetter.core.domain.usecase.debt

import app.cash.turbine.test
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.DebtCreditRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class GetDebtsAndCreditsSummaryUseCaseTest {

    private val debtCreditRepository: DebtCreditRepository = mockk()
    private val useCase = GetDebtsAndCreditsSummaryUseCase(debtCreditRepository)

    @Test
    fun `calculates receivables payables and net position accurately`() = runTest {
        val userId = "user_test_1"
        val items = listOf(
            DebtCredit(
                id = "d1",
                userId = userId,
                direction = DebtDirection.RECEIVABLE,
                personName = "Person A",
                amountMinor = 500000L, // 5,000 BDT
                date = Instant.now(),
                isSettled = false
            ),
            DebtCredit(
                id = "d2",
                userId = userId,
                direction = DebtDirection.RECEIVABLE,
                personName = "Person B",
                amountMinor = 554000L, // 5,540 BDT
                date = Instant.now(),
                isSettled = false
            ),
            DebtCredit(
                id = "d3",
                userId = userId,
                direction = DebtDirection.PAYABLE,
                personName = "Person C",
                amountMinor = 286000L, // 2,860 BDT
                date = Instant.now(),
                isSettled = false
            )
        )

        every { debtCreditRepository.observeDebtsAndCredits(userId) } returns flowOf(items)

        useCase(userId).test {
            val summary = awaitItem()
            // Total receivable = 5,000 + 5,540 = 10,540 BDT = 1,054,000 paisa
            assertEquals(1054000L, summary.totalReceivableMinor)
            // Total payable = 2,860 BDT = 286,000 paisa
            assertEquals(286000L, summary.totalPayableMinor)
            // Net position = 10,540 - 2,860 = 7,680 BDT = 768,000 paisa
            assertEquals(768000L, summary.netPositionMinor)
            assertEquals(2, summary.activeReceivables.size)
            assertEquals(1, summary.activePayables.size)
            assertTrue(summary.settledList.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `excludes deleted items and separates settled items into history`() = runTest {
        val userId = "user_test_2"
        val items = listOf(
            DebtCredit(
                id = "d1",
                userId = userId,
                direction = DebtDirection.RECEIVABLE,
                personName = "Person Active",
                amountMinor = 100000L,
                date = Instant.now(),
                isSettled = false,
                isDeleted = false
            ),
            DebtCredit(
                id = "d2",
                userId = userId,
                direction = DebtDirection.RECEIVABLE,
                personName = "Person Deleted",
                amountMinor = 200000L,
                date = Instant.now(),
                isSettled = false,
                isDeleted = true
            ),
            DebtCredit(
                id = "d3",
                userId = userId,
                direction = DebtDirection.PAYABLE,
                personName = "Person Settled",
                amountMinor = 50000L,
                date = Instant.now(),
                isSettled = true,
                isDeleted = false
            )
        )

        every { debtCreditRepository.observeDebtsAndCredits(userId) } returns flowOf(items)

        useCase(userId).test {
            val summary = awaitItem()
            assertEquals(100000L, summary.totalReceivableMinor)
            assertEquals(0L, summary.totalPayableMinor)
            assertEquals(100000L, summary.netPositionMinor)
            assertEquals(1, summary.activeReceivables.size)
            assertEquals("Person Active", summary.activeReceivables[0].personName)
            assertEquals(0, summary.activePayables.size)
            assertEquals(1, summary.settledList.size)
            assertEquals("Person Settled", summary.settledList[0].personName)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
