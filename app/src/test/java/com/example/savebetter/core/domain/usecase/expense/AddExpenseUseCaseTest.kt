package com.example.savebetter.core.domain.usecase.expense

import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class AddExpenseUseCaseTest {

    private val expenseRepository: ExpenseRepository = mockk(relaxed = true)
    private lateinit var useCase: AddExpenseUseCase

    @Before
    fun setUp() {
        useCase = AddExpenseUseCase(expenseRepository)
    }

    @Test
    fun `invoke saves expense to repository with PENDING syncStatus and non-empty ID`() = runTest {
        val expenseSlot = slot<Expense>()
        coEvery { expenseRepository.addExpense(capture(expenseSlot)) } returns Unit

        val date = Instant.parse("2026-03-15T10:00:00Z")
        val params = AddExpenseParams(
            userId = "user_456",
            amountMinor = 150000L, // ৳1500.00
            categoryId = "cat_household",
            date = date,
            note = "Weekly groceries"
        )

        val result = useCase(params)

        assertTrue(result.isSuccess)
        val returnedExpense = result.getOrThrow()

        coVerify(exactly = 1) { expenseRepository.addExpense(any()) }

        val captured = expenseSlot.captured
        assertEquals("user_456", captured.userId)
        assertEquals(150000L, captured.amountMinor)
        assertEquals("cat_household", captured.categoryId)
        assertEquals(date, captured.date)
        assertEquals("Weekly groceries", captured.note)
        assertEquals(SyncState.PENDING, captured.syncStatus)
        assertEquals(false, captured.isDeleted)
        assertNotNull(captured.id)
        assertTrue(captured.id.isNotBlank())
        assertEquals(captured.id, returnedExpense.id)
    }

    @Test
    fun `invoke trims note and sets null if blank`() = runTest {
        val expenseSlot = slot<Expense>()
        coEvery { expenseRepository.addExpense(capture(expenseSlot)) } returns Unit

        val params = AddExpenseParams(
            userId = "user_456",
            amountMinor = 25000L,
            categoryId = "cat_transport",
            date = Instant.now(),
            note = "   "
        )

        val result = useCase(params)
        assertTrue(result.isSuccess)
        assertNull(expenseSlot.captured.note)
    }
}
