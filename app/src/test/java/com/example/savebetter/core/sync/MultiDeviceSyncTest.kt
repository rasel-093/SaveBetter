package com.example.savebetter.core.sync

import com.example.savebetter.core.data.local.dao.CategoryDao
import com.example.savebetter.core.data.local.dao.DebtCreditDao
import com.example.savebetter.core.data.local.dao.ExpenseDao
import com.example.savebetter.core.data.local.dao.MonthlyTargetDao
import com.example.savebetter.core.data.local.dao.SalaryHandRecordDao
import com.example.savebetter.core.data.local.dao.UserDao
import com.example.savebetter.core.data.local.dao.WeeklyTargetDao
import com.example.savebetter.core.data.local.entity.ExpenseEntity
import com.example.savebetter.core.data.remote.CategoryRemoteDataSource
import com.example.savebetter.core.data.remote.DebtCreditRemoteDataSource
import com.example.savebetter.core.data.remote.ExpenseRemoteDataSource
import com.example.savebetter.core.data.remote.TargetRemoteDataSource
import com.example.savebetter.core.data.remote.UserProfileRemoteDataSource
import com.example.savebetter.core.data.repository.SyncRepositoryImpl
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklyTarget
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests verifying Step 15: Multi-Device Behavior.
 *
 * Simulates multi-device synchronization where the same account operates across
 * multiple physical devices (Phone A and Phone B):
 * - Device B initial login hydrates empty Room from remote Firestore
 * - Two-way update synchronization between Device A and Device B
 * - Tombstone deletion propagation across devices
 * - Concurrent edit resolution via server-authoritative Last-Write-Wins (LWW)
 * - Room remains the UI's local source of truth throughout
 */
class MultiDeviceSyncTest {

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

    private val userId = "user_multi_device_1"
    private val t0 = Instant.parse("2026-09-10T08:00:00Z")
    private val t1 = Instant.parse("2026-09-10T10:00:00Z")
    private val t2 = Instant.parse("2026-09-10T12:00:00Z")

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
    fun `Device B login hydrates empty local Room with all 7 entities from remote`() = runTest {
        // Given: Phone B has an empty Room database for this user
        coEvery { userDao.getUser(userId) } returns null
        coEvery { expenseDao.getExpenseByIdIncludingDeleted(any()) } returns null
        coEvery { categoryDao.getCategoryByIdIncludingDeleted(any()) } returns null
        coEvery { weeklyTargetDao.getWeeklyTargetById(any()) } returns null
        coEvery { monthlyTargetDao.getMonthlyTargetById(any()) } returns null
        coEvery { salaryHandRecordDao.getSalaryHandRecordById(any()) } returns null
        coEvery { debtCreditDao.getDebtCreditByIdIncludingDeleted(any()) } returns null

        // Remote contains data already synced from Phone A
        val remoteProfile = UserProfile(
            id = userId,
            name = "Rasel Ahmed",
            email = "rasel@example.com",
            monthlySalaryMinor = 6500000L,
            onboardingCompleted = true,
            createdAt = t0,
            updatedAt = t0
        )
        val remoteExpense = Expense(
            id = "exp_phone_a_1",
            userId = userId,
            amountMinor = 12000L,
            categoryId = "cat_groceries",
            note = "Rice and oil",
            date = t0,
            createdAt = t0,
            updatedAt = t0,
            syncStatus = SyncState.SYNCED
        )
        val remoteCategory = Category(
            id = "cat_groceries",
            userId = userId,
            customName = "Market Bazar",
            icon = "ic_grocery",
            colorToken = "moss",
            isDefault = false,
            createdAt = t0,
            updatedAt = t0,
            syncStatus = SyncState.SYNCED
        )
        val remoteWeeklyTarget = WeeklyTarget(
            id = "wt_1",
            userId = userId,
            weekStart = "2026-09-07",
            weekEnd = "2026-09-13",
            targetAmountMinor = 150000L,
            updatedAt = t0,
            syncStatus = SyncState.SYNCED
        )
        val remoteMonthlyTarget = MonthlyTarget(
            id = "mt_1",
            userId = userId,
            month = 9,
            year = 2026,
            targetAmountMinor = 600000L,
            savingGoalMinor = 100000L,
            updatedAt = t0,
            syncStatus = SyncState.SYNCED
        )
        val remoteSalaryRecord = SalaryHandRecord(
            id = "shr_1",
            userId = userId,
            month = 9,
            year = 2026,
            salaryAmountMinor = 6500000L,
            handRemainingAmountMinor = 250000L,
            updatedAt = t0,
            syncStatus = SyncState.SYNCED
        )
        val remoteDebt = DebtCredit(
            id = "dc_1",
            userId = userId,
            direction = DebtDirection.RECEIVABLE,
            personName = "Tareq",
            amountMinor = 50000L,
            date = t0,
            createdAt = t0,
            updatedAt = t0,
            syncStatus = SyncState.SYNCED
        )

        coEvery { userProfileRemoteDataSource.fetchUserProfile(userId) } returns Result.success(remoteProfile)
        coEvery { expenseRemoteDataSource.fetchExpenses(userId) } returns Result.success(listOf(remoteExpense))
        coEvery { categoryRemoteDataSource.fetchCategories(userId) } returns Result.success(listOf(remoteCategory))
        coEvery { targetRemoteDataSource.fetchWeeklyTargets(userId) } returns Result.success(listOf(remoteWeeklyTarget))
        coEvery { targetRemoteDataSource.fetchMonthlyTargets(userId) } returns Result.success(listOf(remoteMonthlyTarget))
        coEvery { targetRemoteDataSource.fetchSalaryHandRecords(userId) } returns Result.success(listOf(remoteSalaryRecord))
        coEvery { debtCreditRemoteDataSource.fetchDebtsAndCredits(userId) } returns Result.success(listOf(remoteDebt))

        // When: Initial pull sync is executed on Phone B
        val result = syncRepository.pullRemote(userId)

        // Then: All 7 entities are saved into Phone B's Room database with SYNCED status
        assertTrue(result.isSuccess)
        coVerify { userDao.upsertUser(match { it.id == userId && it.onboardingCompleted && it.syncStatus == SyncState.SYNCED }) }
        coVerify { expenseDao.insertExpense(match { it.id == "exp_phone_a_1" && it.syncStatus == SyncState.SYNCED }) }
        coVerify { categoryDao.insertCategory(match { it.id == "cat_groceries" && it.syncStatus == SyncState.SYNCED }) }
        coVerify { weeklyTargetDao.upsertWeeklyTarget(match { it.id == "wt_1" && it.syncStatus == SyncState.SYNCED }) }
        coVerify { monthlyTargetDao.upsertMonthlyTarget(match { it.id == "mt_1" && it.syncStatus == SyncState.SYNCED }) }
        coVerify { salaryHandRecordDao.upsertSalaryHandRecord(match { it.id == "shr_1" && it.syncStatus == SyncState.SYNCED }) }
        coVerify { debtCreditDao.upsertDebtCredit(match { it.id == "dc_1" && it.syncStatus == SyncState.SYNCED }) }
    }

    @Test
    fun `Device A updates an expense and Device B syncs the newer modification`() = runTest {
        // Given: Phone B already has older local expense from t0
        val localOnPhoneB = ExpenseEntity(
            id = "exp_shared",
            userId = userId,
            amountMinor = 10000L,
            categoryId = "cat_food",
            note = "Old note",
            date = t0,
            createdAt = t0,
            updatedAt = t0,
            syncStatus = SyncState.SYNCED
        )
        coEvery { expenseDao.getExpenseByIdIncludingDeleted("exp_shared") } returns localOnPhoneB

        // Remote has Phone A's update made at t1
        val remoteFromPhoneA = Expense(
            id = "exp_shared",
            userId = userId,
            amountMinor = 15000L, // Phone A updated amount
            categoryId = "cat_food",
            note = "Phone A modified note",
            date = t0,
            createdAt = t0,
            updatedAt = t1, // Strictly newer
            syncStatus = SyncState.SYNCED
        )

        coEvery { userProfileRemoteDataSource.fetchUserProfile(userId) } returns Result.success(null)
        coEvery { expenseRemoteDataSource.fetchExpenses(userId) } returns Result.success(listOf(remoteFromPhoneA))
        coEvery { categoryRemoteDataSource.fetchCategories(userId) } returns Result.success(emptyList())
        coEvery { targetRemoteDataSource.fetchWeeklyTargets(userId) } returns Result.success(emptyList())
        coEvery { targetRemoteDataSource.fetchMonthlyTargets(userId) } returns Result.success(emptyList())
        coEvery { targetRemoteDataSource.fetchSalaryHandRecords(userId) } returns Result.success(emptyList())
        coEvery { debtCreditRemoteDataSource.fetchDebtsAndCredits(userId) } returns Result.success(emptyList())

        // When: Phone B pulls from remote
        val result = syncRepository.pullRemote(userId)

        // Then: Phone B Room is updated with Phone A's newer version
        assertTrue(result.isSuccess)
        coVerify {
            expenseDao.insertExpense(match {
                it.id == "exp_shared" &&
                        it.amountMinor == 15000L &&
                        it.note == "Phone A modified note" &&
                        it.syncStatus == SyncState.SYNCED
            })
        }
    }

    @Test
    fun `Device A soft-deletes a record and tombstone propagates to Device B`() = runTest {
        // Given: Phone B has an active Debt record
        val phoneBActiveDebt = DebtCredit(
            id = "debt_shared",
            userId = userId,
            direction = DebtDirection.PAYABLE,
            personName = "Shopkeeper",
            amountMinor = 20000L,
            date = t0,
            createdAt = t0,
            updatedAt = t0,
            syncStatus = SyncState.SYNCED,
            isDeleted = false
        )

        // Phone A soft-deleted the debt at t2
        val phoneADeletedDebt = phoneBActiveDebt.copy(
            isDeleted = true,
            deletedAt = t2,
            updatedAt = t2
        )

        // When evaluating conflict resolution on Phone B
        val resolution = SyncConflictResolver.resolveDebtCredit(phoneBActiveDebt, phoneADeletedDebt)

        // Then: Conflict resolution chooses remote tombstone
        assertEquals(ConflictResolution.USE_REMOTE, resolution)
    }

    @Test
    fun `Concurrent edits on Device A and Device B resolve via Last-Write-Wins`() {
        // Device A updated category at t1 (10:00 AM)
        val deviceAUpdate = Category(
            id = "cat_shared",
            userId = userId,
            customName = "Utility Bills - Phone A",
            icon = "ic_bills",
            colorToken = "clay",
            isDefault = false,
            createdAt = t0,
            updatedAt = t1,
            syncStatus = SyncState.PENDING
        )

        // Device B updated the same category at t2 (12:00 PM - newer)
        val deviceBUpdate = Category(
            id = "cat_shared",
            userId = userId,
            customName = "Utilities & Internet - Phone B",
            icon = "ic_bills",
            colorToken = "brick",
            isDefault = false,
            createdAt = t0,
            updatedAt = t2,
            syncStatus = SyncState.SYNCED
        )

        // Conflict resolution favors the newer update from Device B
        val resolution = SyncConflictResolver.resolveCategory(deviceAUpdate, deviceBUpdate)
        assertEquals(ConflictResolution.USE_REMOTE, resolution)
    }
}
