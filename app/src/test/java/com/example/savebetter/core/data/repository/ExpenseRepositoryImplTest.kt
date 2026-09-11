package com.example.savebetter.core.data.repository

import app.cash.turbine.test
import com.example.savebetter.core.data.local.dao.ExpenseDao
import com.example.savebetter.core.data.mapper.toEntity
import com.example.savebetter.core.data.remote.ExpenseRemoteDataSource
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.SyncState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for [ExpenseRepositoryImpl] verifying local-source-of-truth semantics.
 */
class ExpenseRepositoryImplTest {

    private val expenseDao: ExpenseDao = mockk(relaxed = true)
    private val remoteDataSource: ExpenseRemoteDataSource = mockk(relaxed = true)
    private val repository = ExpenseRepositoryImpl(expenseDao, remoteDataSource)

    private val testExpense = Expense(
        id = "exp_test_1",
        userId = "user_123",
        amountMinor = 25000L,
        categoryId = "cat_grocery",
        note = "Milk and tea",
        date = Instant.now()
    )

    @Test
    fun `addExpense writes to Room immediately with PENDING status`() = runTest {
        coEvery { remoteDataSource.uploadExpense(any()) } returns Result.success(Unit)

        repository.addExpense(testExpense)

        // Verifies Room insert was called
        coVerify {
            expenseDao.insertExpense(match { entity ->
                entity.id == testExpense.id && entity.syncStatus == SyncState.PENDING
            })
        }
        // Verifies remote upload was dispatched
        coVerify { remoteDataSource.uploadExpense(any()) }
    }

    @Test
    fun `addExpense keeps PENDING in Room if remote upload fails`() = runTest {
        coEvery { remoteDataSource.uploadExpense(any()) } returns Result.failure(Exception("Network unavailable"))

        repository.addExpense(testExpense)

        // Verifies Room insert still happened with PENDING
        coVerify {
            expenseDao.insertExpense(match { entity ->
                entity.id == testExpense.id && entity.syncStatus == SyncState.PENDING
            })
        }
        // updateExpense to SYNCED was not called
        coVerify(exactly = 0) {
            expenseDao.updateExpense(match { it.syncStatus == SyncState.SYNCED })
        }
    }

    @Test
    fun `observeExpenses returns Flow from DAO mapped to domain`() = runTest {
        val entityList = listOf(testExpense.toEntity())
        every { expenseDao.observeExpenses("user_123") } returns flowOf(entityList)

        repository.observeExpenses("user_123").test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(testExpense.id, list[0].id)
            awaitComplete()
        }
    }

    @Test
    fun `deleteExpense sets isDeleted true and syncs deletion`() = runTest {
        coEvery { expenseDao.getExpenseById("exp_test_1") } returns testExpense.toEntity()
        coEvery { remoteDataSource.deleteExpense(any(), any()) } returns Result.success(Unit)

        repository.deleteExpense("exp_test_1")

        coVerify {
            expenseDao.softDeleteExpense(
                id = "exp_test_1",
                deletedAt = any(),
                updatedAt = any(),
                syncStatus = SyncState.PENDING
            )
        }
        coVerify { remoteDataSource.deleteExpense("user_123", "exp_test_1") }
    }
}
