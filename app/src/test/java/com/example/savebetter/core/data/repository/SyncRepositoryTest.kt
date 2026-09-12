package com.example.savebetter.core.data.repository

import app.cash.turbine.test
import com.example.savebetter.core.data.local.dao.CategoryDao
import com.example.savebetter.core.data.local.dao.DebtCreditDao
import com.example.savebetter.core.data.local.dao.ExpenseDao
import com.example.savebetter.core.data.local.dao.MonthlyTargetDao
import com.example.savebetter.core.data.local.dao.SalaryHandRecordDao
import com.example.savebetter.core.data.local.dao.UserDao
import com.example.savebetter.core.data.local.dao.WeeklyTargetDao
import com.example.savebetter.core.data.local.entity.ExpenseEntity
import com.example.savebetter.core.data.local.entity.UserEntity
import com.example.savebetter.core.data.remote.CategoryRemoteDataSource
import com.example.savebetter.core.data.remote.DebtCreditRemoteDataSource
import com.example.savebetter.core.data.remote.ExpenseRemoteDataSource
import com.example.savebetter.core.data.remote.TargetRemoteDataSource
import com.example.savebetter.core.data.remote.UserProfileRemoteDataSource
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for [SyncRepositoryImpl] verifying 2-way sync orchestrations.
 */
class SyncRepositoryTest {

    private val userDao: UserDao = mockk(relaxed = true)
    private val expenseDao: ExpenseDao = mockk(relaxed = true)
    private val categoryDao: CategoryDao = mockk(relaxed = true)
    private val weeklyTargetDao: WeeklyTargetDao = mockk(relaxed = true)
    private val monthlyTargetDao: MonthlyTargetDao = mockk(relaxed = true)
    private val salaryHandRecordDao: SalaryHandRecordDao = mockk(relaxed = true)
    private val debtCreditDao: DebtCreditDao = mockk(relaxed = true)

    private val userProfileRemoteDataSource: UserProfileRemoteDataSource = mockk(relaxed = true)
    private val expenseRemoteDataSource: ExpenseRemoteDataSource = mockk(relaxed = true)
    private val categoryRemoteDataSource: CategoryRemoteDataSource = mockk(relaxed = true)
    private val targetRemoteDataSource: TargetRemoteDataSource = mockk(relaxed = true)
    private val debtCreditRemoteDataSource: DebtCreditRemoteDataSource = mockk(relaxed = true)

    private lateinit var syncRepository: SyncRepositoryImpl

    private val userId = "user_test_1"
    private val now = Instant.parse("2026-09-01T12:00:00Z")

    @Before
    fun setUp() {
        syncRepository = SyncRepositoryImpl(
            userDao = userDao,
            expenseDao = expenseDao,
            categoryDao = categoryDao,
            weeklyTargetDao = weeklyTargetDao,
            monthlyTargetDao = monthlyTargetDao,
            salaryHandRecordDao = salaryHandRecordDao,
            debtCreditDao = debtCreditDao,
            userProfileRemoteDataSource = userProfileRemoteDataSource,
            expenseRemoteDataSource = expenseRemoteDataSource,
            categoryRemoteDataSource = categoryRemoteDataSource,
            targetRemoteDataSource = targetRemoteDataSource,
            debtCreditRemoteDataSource = debtCreditRemoteDataSource
        )
    }

    @Test
    fun `pushPending uploads pending user profile and marks SYNCED`() = runTest {
        val pendingUser = UserEntity(
            id = userId,
            name = "Test User",
            email = "test@example.com",
            monthlySalaryMinor = 5000000L,
            preferredLanguage = "en",
            onboardingCompleted = true,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncState.PENDING
        )

        coEvery { userDao.getUser(userId) } returns pendingUser
        coEvery { userProfileRemoteDataSource.saveUserProfile(any()) } returns Result.success(Unit)

        val result = syncRepository.pushPending(userId)

        assertTrue(result.isSuccess)
        coVerify { userProfileRemoteDataSource.saveUserProfile(match { it.id == userId }) }
        coVerify { userDao.updateSyncStatus(userId, SyncState.SYNCED) }
    }

    @Test
    fun `pushPending uploads pending expenses and marks them SYNCED`() = runTest {
        val pendingExpense = ExpenseEntity(
            id = "exp_1",
            userId = userId,
            amountMinor = 35000L,
            categoryId = "cat_food",
            note = "Groceries",
            date = now,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncState.PENDING
        )

        coEvery { expenseDao.getPendingExpenses(userId) } returns listOf(pendingExpense)
        coEvery { expenseRemoteDataSource.uploadExpense(any()) } returns Result.success(Unit)

        val result = syncRepository.pushPending(userId)

        assertTrue(result.isSuccess)
        coVerify { expenseRemoteDataSource.uploadExpense(match { it.id == "exp_1" }) }
        coVerify { expenseDao.updateSyncStatus("exp_1", SyncState.SYNCED) }
    }

    @Test
    fun `pullRemote applies newer remote expenses and inserts them with SYNCED status`() = runTest {
        val remoteExpense = Expense(
            id = "exp_2",
            userId = userId,
            amountMinor = 45000L,
            categoryId = "cat_transport",
            note = "Bus ride",
            date = now,
            createdAt = now,
            updatedAt = now.plusSeconds(3600), // Newer
            syncStatus = SyncState.SYNCED
        )

        val localExpense = ExpenseEntity(
            id = "exp_2",
            userId = userId,
            amountMinor = 40000L,
            categoryId = "cat_transport",
            note = "Bus ride old",
            date = now,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncState.PENDING
        )

        coEvery { userProfileRemoteDataSource.fetchUserProfile(userId) } returns Result.success(null)
        coEvery { expenseRemoteDataSource.fetchExpenses(userId) } returns Result.success(listOf(remoteExpense))
        coEvery { expenseDao.getExpenseByIdIncludingDeleted("exp_2") } returns localExpense
        coEvery { categoryRemoteDataSource.fetchCategories(userId) } returns Result.success(emptyList())
        coEvery { targetRemoteDataSource.fetchWeeklyTargets(userId) } returns Result.success(emptyList())
        coEvery { targetRemoteDataSource.fetchMonthlyTargets(userId) } returns Result.success(emptyList())
        coEvery { targetRemoteDataSource.fetchSalaryHandRecords(userId) } returns Result.success(emptyList())
        coEvery { debtCreditRemoteDataSource.fetchDebtsAndCredits(userId) } returns Result.success(emptyList())

        val result = syncRepository.pullRemote(userId)

        assertTrue(result.isSuccess)
        coVerify {
            expenseDao.insertExpense(match {
                it.id == "exp_2" && it.amountMinor == 45000L && it.syncStatus == SyncState.SYNCED
            })
        }
    }

    @Test
    fun `observeTotalPendingCount sums pending counts from all 7 DAOs`() = runTest {
        every { userDao.observePendingCount(userId) } returns flowOf(1)
        every { expenseDao.observePendingCount(userId) } returns flowOf(2)
        every { categoryDao.observePendingCount(userId) } returns flowOf(1)
        every { weeklyTargetDao.observePendingCount(userId) } returns flowOf(0)
        every { monthlyTargetDao.observePendingCount(userId) } returns flowOf(1)
        every { salaryHandRecordDao.observePendingCount(userId) } returns flowOf(0)
        every { debtCreditDao.observePendingCount(userId) } returns flowOf(3)

        syncRepository.observeTotalPendingCount(userId).test {
            val total = awaitItem()
            assertEquals(8, total) // 1 + 2 + 1 + 0 + 1 + 0 + 3 = 8
            cancelAndIgnoreRemainingEvents()
        }
    }
}
