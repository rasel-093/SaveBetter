package com.example.savebetter.core.domain.usecase.expense

import com.example.savebetter.core.domain.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteExpenseUseCaseTest {

    private val expenseRepository: ExpenseRepository = mockk(relaxed = true)
    private lateinit var useCase: DeleteExpenseUseCase

    @Before
    fun setUp() {
        useCase = DeleteExpenseUseCase(expenseRepository)
    }

    @Test
    fun `invoke calls deleteExpense on repository`() = runTest {
        coEvery { expenseRepository.deleteExpense("exp_123") } returns Unit

        val result = useCase("exp_123")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { expenseRepository.deleteExpense("exp_123") }
    }

    @Test
    fun `invoke returns failure when repository throws`() = runTest {
        coEvery { expenseRepository.deleteExpense("exp_fail") } throws RuntimeException("DB error")

        val result = useCase("exp_fail")

        assertTrue(result.isFailure)
    }
}
