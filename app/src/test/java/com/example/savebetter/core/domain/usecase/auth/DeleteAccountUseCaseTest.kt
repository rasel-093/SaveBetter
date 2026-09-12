package com.example.savebetter.core.domain.usecase.auth

import android.content.Context
import com.example.savebetter.core.auth.model.RecentLoginRequiredException
import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.data.local.SaveBetterDatabase
import com.example.savebetter.core.data.local.SettingsPreferences
import com.example.savebetter.core.data.local.dao.CategoryDao
import com.example.savebetter.core.data.local.dao.DebtCreditDao
import com.example.savebetter.core.data.local.dao.ExpenseDao
import com.example.savebetter.core.data.local.dao.MonthlyTargetDao
import com.example.savebetter.core.data.local.dao.SalaryHandRecordDao
import com.example.savebetter.core.data.local.dao.UserDao
import com.example.savebetter.core.data.local.dao.WeeklyTargetDao
import com.example.savebetter.core.domain.repository.UserProfileRepository
import com.example.savebetter.core.notification.ReconciliationReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteAccountUseCaseTest {

    private val authRepository: AuthRepository = mockk()
    private val userProfileRepository: UserProfileRepository = mockk()
    private val settingsPreferences: SettingsPreferences = mockk()
    private val database: SaveBetterDatabase = mockk()
    private val context: Context = mockk(relaxed = true)

    private val userDao: UserDao = mockk(relaxed = true)
    private val expenseDao: ExpenseDao = mockk(relaxed = true)
    private val categoryDao: CategoryDao = mockk(relaxed = true)
    private val weeklyTargetDao: WeeklyTargetDao = mockk(relaxed = true)
    private val monthlyTargetDao: MonthlyTargetDao = mockk(relaxed = true)
    private val salaryHandRecordDao: SalaryHandRecordDao = mockk(relaxed = true)
    private val debtCreditDao: DebtCreditDao = mockk(relaxed = true)

    private lateinit var useCase: DeleteAccountUseCase

    @Before
    fun setUp() {
        mockkObject(ReconciliationReminderScheduler)
        every { ReconciliationReminderScheduler.cancelAllUserWork(any()) } returns Unit

        every { database.userDao() } returns userDao
        every { database.expenseDao() } returns expenseDao
        every { database.categoryDao() } returns categoryDao
        every { database.weeklyTargetDao() } returns weeklyTargetDao
        every { database.monthlyTargetDao() } returns monthlyTargetDao
        every { database.salaryHandRecordDao() } returns salaryHandRecordDao
        every { database.debtCreditDao() } returns debtCreditDao
        every { database.clearAllTables() } returns Unit

        coEvery { settingsPreferences.clearAllPreferences() } returns Unit

        useCase = DeleteAccountUseCase(
            authRepository = authRepository,
            userProfileRepository = userProfileRepository,
            settingsPreferences = settingsPreferences,
            database = database,
            context = context
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke deletes remote user data, auth account, local tables, preferences, and cancels work`() = runTest {
        coEvery { userProfileRepository.deleteUserData("user_123") } returns Result.success(Unit)
        coEvery { authRepository.deleteAccount() } returns Result.success(Unit)

        val result = useCase("user_123")

        assertTrue(result.isSuccess)
        coVerify { userProfileRepository.deleteUserData("user_123") }
        coVerify { authRepository.deleteAccount() }
        coVerify { userDao.deleteUser("user_123") }
        coVerify { expenseDao.deleteExpensesByUserId("user_123") }
        coVerify { categoryDao.deleteCategoriesByUserId("user_123") }
        coVerify { weeklyTargetDao.deleteWeeklyTargetsByUserId("user_123") }
        coVerify { monthlyTargetDao.deleteMonthlyTargetsByUserId("user_123") }
        coVerify { salaryHandRecordDao.deleteSalaryHandRecordsByUserId("user_123") }
        coVerify { debtCreditDao.deleteDebtCreditsByUserId("user_123") }
        coVerify { database.clearAllTables() }
        coVerify { settingsPreferences.clearAllPreferences() }
        coVerify { ReconciliationReminderScheduler.cancelAllUserWork(context) }
    }

    @Test
    fun `invoke with password reauthenticates before deletion`() = runTest {
        coEvery { authRepository.reauthenticate("myPassword123") } returns Result.success(Unit)
        coEvery { userProfileRepository.deleteUserData("user_123") } returns Result.success(Unit)
        coEvery { authRepository.deleteAccount() } returns Result.success(Unit)

        val result = useCase("user_123", password = "myPassword123")

        assertTrue(result.isSuccess)
        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            authRepository.reauthenticate("myPassword123")
            userProfileRepository.deleteUserData("user_123")
            authRepository.deleteAccount()
            database.clearAllTables()
        }
    }

    @Test
    fun `invoke when reauthentication fails returns failure without clearing local data`() = runTest {
        coEvery { authRepository.reauthenticate("wrongPassword") } returns Result.failure(
            IllegalArgumentException("Invalid password")
        )

        val result = useCase("user_123", password = "wrongPassword")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { userProfileRepository.deleteUserData(any()) }
        coVerify(exactly = 0) { authRepository.deleteAccount() }
        coVerify(exactly = 0) { database.clearAllTables() }
    }

    @Test
    fun `invoke propagates RecentLoginRequiredException when auth requires recent login`() = runTest {
        coEvery { userProfileRepository.deleteUserData("user_123") } returns Result.success(Unit)
        coEvery { authRepository.deleteAccount() } returns Result.failure(
            RecentLoginRequiredException("Recent login required")
        )

        val result = useCase("user_123")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RecentLoginRequiredException)
        coVerify(exactly = 0) { database.clearAllTables() }
    }
}
