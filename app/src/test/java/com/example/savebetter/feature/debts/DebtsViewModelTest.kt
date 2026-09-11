package com.example.savebetter.feature.debts

import app.cash.turbine.test
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtCreditSummary
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.usecase.debt.AddDebtCreditUseCase
import com.example.savebetter.core.domain.usecase.debt.DeleteDebtCreditUseCase
import com.example.savebetter.core.domain.usecase.debt.GetDebtsAndCreditsSummaryUseCase
import com.example.savebetter.core.domain.usecase.debt.SettleDebtCreditUseCase
import com.example.savebetter.core.domain.usecase.debt.UpdateDebtCreditUseCase
import com.example.savebetter.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class DebtsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private val getDebtsAndCreditsSummaryUseCase: GetDebtsAndCreditsSummaryUseCase = mockk()
    private val addDebtCreditUseCase: AddDebtCreditUseCase = mockk()
    private val updateDebtCreditUseCase: UpdateDebtCreditUseCase = mockk()
    private val settleDebtCreditUseCase: SettleDebtCreditUseCase = mockk()
    private val deleteDebtCreditUseCase: DeleteDebtCreditUseCase = mockk()

    private lateinit var viewModel: DebtsViewModel

    private val testUser = AuthUser(
        id = "user_debts_1",
        email = "test@example.com",
        displayName = "Test User"
    )

    private val testItem = DebtCredit(
        id = "debt_1",
        userId = "user_debts_1",
        direction = DebtDirection.RECEIVABLE,
        personName = "Rahim",
        amountMinor = 500000L,
        date = Instant.now(),
        isSettled = false
    )

    private val testSummary = DebtCreditSummary(
        totalReceivableMinor = 500000L,
        totalPayableMinor = 200000L,
        netPositionMinor = 300000L,
        activeReceivables = listOf(testItem),
        activePayables = emptyList(),
        settledList = emptyList()
    )

    @Before
    fun setUp() {
        every { authRepository.observeAuthState() } returns flowOf(testUser)
        every { getDebtsAndCreditsSummaryUseCase("user_debts_1") } returns flowOf(testSummary)

        viewModel = DebtsViewModel(
            authRepository = authRepository,
            getDebtsAndCreditsSummaryUseCase = getDebtsAndCreditsSummaryUseCase,
            addDebtCreditUseCase = addDebtCreditUseCase,
            updateDebtCreditUseCase = updateDebtCreditUseCase,
            settleDebtCreditUseCase = settleDebtCreditUseCase,
            deleteDebtCreditUseCase = deleteDebtCreditUseCase
        )
    }

    @Test
    fun `uiState loads debt summary for authenticated user`() = runTest {
        viewModel.initUser("user_debts_1")
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }
            assertFalse(state.isLoading)
            assertEquals(500000L, state.summary.totalReceivableMinor)
            assertEquals(200000L, state.summary.totalPayableMinor)
            assertEquals(300000L, state.summary.netPositionMinor)
            assertEquals(1, state.summary.activeReceivables.size)
            assertEquals("Rahim", state.summary.activeReceivables[0].personName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `tab selection toggles between ACTIVE and HISTORY`() = runTest {
        viewModel.initUser("user_debts_1")
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }
            assertEquals(DebtTab.ACTIVE, state.selectedTab)

            viewModel.selectTab(DebtTab.HISTORY)
            assertEquals(DebtTab.HISTORY, awaitItem().selectedTab)

            viewModel.selectTab(DebtTab.ACTIVE)
            assertEquals(DebtTab.ACTIVE, awaitItem().selectedTab)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openAddDialog and openEditDialog manage dialog state`() = runTest {
        viewModel.initUser("user_debts_1")
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            var initial = awaitItem()
            if (initial.isLoading) {
                initial = awaitItem()
            }
            assertFalse(initial.isAddEditSheetOpen)
            assertNull(initial.editingItem)

            viewModel.openAddDialog()
            val addState = awaitItem()
            assertTrue(addState.isAddEditSheetOpen)
            assertNull(addState.editingItem)

            viewModel.openEditDialog(testItem)
            val editState = awaitItem()
            assertTrue(editState.isAddEditSheetOpen)
            assertEquals(testItem, editState.editingItem)

            viewModel.dismissDialog()
            val dismissedState = awaitItem()
            assertFalse(dismissedState.isAddEditSheetOpen)
            assertNull(dismissedState.editingItem)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveDebtCredit calls addDebtCreditUseCase when not editing`() = runTest {
        viewModel.initUser("user_debts_1")
        viewModel.openAddDialog()

        coEvery {
            addDebtCreditUseCase(
                userId = "user_debts_1",
                direction = DebtDirection.RECEIVABLE,
                personName = "Karim",
                amountMinor = 250000L,
                note = "Advance",
                date = any(),
                dueDate = null
            )
        } returns Result.success(testItem.copy(id = "debt_2", personName = "Karim", amountMinor = 250000L))

        viewModel.saveDebtCredit(
            direction = DebtDirection.RECEIVABLE,
            personName = "Karim",
            amountMinor = 250000L,
            note = "Advance",
            dueDate = null
        )
        testScheduler.advanceUntilIdle()

        coVerify {
            addDebtCreditUseCase(
                userId = "user_debts_1",
                direction = DebtDirection.RECEIVABLE,
                personName = "Karim",
                amountMinor = 250000L,
                note = "Advance",
                date = any(),
                dueDate = null
            )
        }
    }

    @Test
    fun `toggleSettle calls settleDebtCreditUseCase`() = runTest {
        viewModel.initUser("user_debts_1")
        coEvery { settleDebtCreditUseCase("debt_1", true) } returns Result.success(Unit)

        viewModel.toggleSettle(testItem)
        testScheduler.advanceUntilIdle()

        coVerify { settleDebtCreditUseCase("debt_1", true) }
    }

    @Test
    fun `deleteItem calls deleteDebtCreditUseCase`() = runTest {
        viewModel.initUser("user_debts_1")
        coEvery { deleteDebtCreditUseCase("debt_1") } returns Result.success(Unit)

        viewModel.deleteItem(testItem)
        testScheduler.advanceUntilIdle()

        coVerify { deleteDebtCreditUseCase("debt_1") }
    }
}
