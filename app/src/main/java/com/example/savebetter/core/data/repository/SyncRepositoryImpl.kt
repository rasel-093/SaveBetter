package com.example.savebetter.core.data.repository

import com.example.savebetter.core.data.local.dao.CategoryDao
import com.example.savebetter.core.data.local.dao.DebtCreditDao
import com.example.savebetter.core.data.local.dao.ExpenseDao
import com.example.savebetter.core.data.local.dao.MonthlyTargetDao
import com.example.savebetter.core.data.local.dao.SalaryHandRecordDao
import com.example.savebetter.core.data.local.dao.UserDao
import com.example.savebetter.core.data.local.dao.WeeklyTargetDao
import com.example.savebetter.core.data.mapper.toDomain
import com.example.savebetter.core.data.mapper.toEntity
import com.example.savebetter.core.data.remote.CategoryRemoteDataSource
import com.example.savebetter.core.data.remote.DebtCreditRemoteDataSource
import com.example.savebetter.core.data.remote.ExpenseRemoteDataSource
import com.example.savebetter.core.data.remote.TargetRemoteDataSource
import com.example.savebetter.core.data.remote.UserProfileRemoteDataSource
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.SyncRepository
import com.example.savebetter.core.sync.SyncConflictResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [SyncRepository] that orchestrates 2-way synchronization
 * across all 7 entities in SaveBetter with Last-Write-Wins conflict resolution.
 */
@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val weeklyTargetDao: WeeklyTargetDao,
    private val monthlyTargetDao: MonthlyTargetDao,
    private val salaryHandRecordDao: SalaryHandRecordDao,
    private val debtCreditDao: DebtCreditDao,
    private val userProfileRemoteDataSource: UserProfileRemoteDataSource,
    private val expenseRemoteDataSource: ExpenseRemoteDataSource,
    private val categoryRemoteDataSource: CategoryRemoteDataSource,
    private val targetRemoteDataSource: TargetRemoteDataSource,
    private val debtCreditRemoteDataSource: DebtCreditRemoteDataSource
) : SyncRepository {

    override suspend fun syncAll(userId: String): Result<Unit> = runCatching {
        pushPending(userId).getOrThrow()
        pullRemote(userId).getOrThrow()
    }

    override suspend fun pushPending(userId: String): Result<Unit> = runCatching {
        // 1. User profile
        val localUser = userDao.getUser(userId)
        if (localUser != null && localUser.syncStatus != SyncState.SYNCED) {
            userProfileRemoteDataSource.saveUserProfile(localUser.toDomain()).getOrThrow()
            userDao.updateSyncStatus(userId, SyncState.SYNCED)
        }

        // 2. Expenses
        val pendingExpenses = expenseDao.getPendingExpenses(userId)
        for (expense in pendingExpenses) {
            expenseRemoteDataSource.uploadExpense(expense.toDomain()).getOrThrow()
            expenseDao.updateSyncStatus(expense.id, SyncState.SYNCED)
        }

        // 3. Categories
        val pendingCategories = categoryDao.getPendingCategories(userId)
        for (category in pendingCategories) {
            categoryRemoteDataSource.uploadCategory(category.toDomain()).getOrThrow()
            categoryDao.updateSyncStatus(category.id, SyncState.SYNCED)
        }

        // 4. Weekly targets
        val pendingWeeklyTargets = weeklyTargetDao.getPendingWeeklyTargets(userId)
        for (target in pendingWeeklyTargets) {
            targetRemoteDataSource.uploadWeeklyTarget(target.toDomain()).getOrThrow()
            weeklyTargetDao.updateSyncStatus(target.id, SyncState.SYNCED)
        }

        // 5. Monthly targets
        val pendingMonthlyTargets = monthlyTargetDao.getPendingMonthlyTargets(userId)
        for (target in pendingMonthlyTargets) {
            targetRemoteDataSource.uploadMonthlyTarget(target.toDomain()).getOrThrow()
            monthlyTargetDao.updateSyncStatus(target.id, SyncState.SYNCED)
        }

        // 6. Salary hand records
        val pendingSalaryRecords = salaryHandRecordDao.getPendingSalaryHandRecords(userId)
        for (record in pendingSalaryRecords) {
            targetRemoteDataSource.uploadSalaryHandRecord(record.toDomain()).getOrThrow()
            salaryHandRecordDao.updateSyncStatus(record.id, SyncState.SYNCED)
        }

        // 7. Debts and credits
        val pendingDebts = debtCreditDao.getPendingDebtsAndCredits(userId)
        for (debt in pendingDebts) {
            debtCreditRemoteDataSource.uploadDebtCredit(debt.toDomain()).getOrThrow()
            debtCreditDao.updateSyncStatus(debt.id, SyncState.SYNCED)
        }
    }

    override suspend fun pullRemote(userId: String): Result<Unit> = runCatching {
        // 1. User profile
        val remoteUserResult = userProfileRemoteDataSource.fetchUserProfile(userId)
        val remoteUser = remoteUserResult.getOrThrow()
        if (remoteUser != null) {
            val localUser = userDao.getUser(userId)?.toDomain()
            val resolution = SyncConflictResolver.resolveUser(localUser, remoteUser)
            if (resolution == com.example.savebetter.core.sync.ConflictResolution.USE_REMOTE) {
                userDao.upsertUser(remoteUser.copy(syncStatus = SyncState.SYNCED).toEntity())
            }
        }

        // 2. Expenses
        val remoteExpenses = expenseRemoteDataSource.fetchExpenses(userId).getOrThrow()
        for (remote in remoteExpenses) {
            val local = expenseDao.getExpenseByIdIncludingDeleted(remote.id)?.toDomain()
            val resolution = SyncConflictResolver.resolveExpense(local, remote)
            if (resolution == com.example.savebetter.core.sync.ConflictResolution.USE_REMOTE) {
                expenseDao.insertExpense(remote.copy(syncStatus = SyncState.SYNCED).toEntity())
            }
        }

        // 3. Categories
        val remoteCategories = categoryRemoteDataSource.fetchCategories(userId).getOrThrow()
        for (remote in remoteCategories) {
            val local = categoryDao.getCategoryByIdIncludingDeleted(remote.id)?.toDomain()
            val resolution = SyncConflictResolver.resolveCategory(local, remote)
            if (resolution == com.example.savebetter.core.sync.ConflictResolution.USE_REMOTE) {
                categoryDao.insertCategory(remote.copy(syncStatus = SyncState.SYNCED).toEntity())
            }
        }

        // 4. Weekly targets
        val remoteWeeklyTargets = targetRemoteDataSource.fetchWeeklyTargets(userId).getOrThrow()
        for (remote in remoteWeeklyTargets) {
            val local = weeklyTargetDao.getWeeklyTargetById(remote.id)?.toDomain()
            val resolution = SyncConflictResolver.resolveWeeklyTarget(local, remote)
            if (resolution == com.example.savebetter.core.sync.ConflictResolution.USE_REMOTE) {
                weeklyTargetDao.upsertWeeklyTarget(remote.copy(syncStatus = SyncState.SYNCED).toEntity())
            }
        }

        // 5. Monthly targets
        val remoteMonthlyTargets = targetRemoteDataSource.fetchMonthlyTargets(userId).getOrThrow()
        for (remote in remoteMonthlyTargets) {
            val local = monthlyTargetDao.getMonthlyTargetById(remote.id)?.toDomain()
            val resolution = SyncConflictResolver.resolveMonthlyTarget(local, remote)
            if (resolution == com.example.savebetter.core.sync.ConflictResolution.USE_REMOTE) {
                monthlyTargetDao.upsertMonthlyTarget(remote.copy(syncStatus = SyncState.SYNCED).toEntity())
            }
        }

        // 6. Salary hand records
        val remoteSalaryRecords = targetRemoteDataSource.fetchSalaryHandRecords(userId).getOrThrow()
        for (remote in remoteSalaryRecords) {
            val local = salaryHandRecordDao.getSalaryHandRecordById(remote.id)?.toDomain()
            val resolution = SyncConflictResolver.resolveSalaryHandRecord(local, remote)
            if (resolution == com.example.savebetter.core.sync.ConflictResolution.USE_REMOTE) {
                salaryHandRecordDao.upsertSalaryHandRecord(remote.copy(syncStatus = SyncState.SYNCED).toEntity())
            }
        }

        // 7. Debts and credits
        val remoteDebts = debtCreditRemoteDataSource.fetchDebtsAndCredits(userId).getOrThrow()
        for (remote in remoteDebts) {
            val local = debtCreditDao.getDebtCreditByIdIncludingDeleted(remote.id)?.toDomain()
            val resolution = SyncConflictResolver.resolveDebtCredit(local, remote)
            if (resolution == com.example.savebetter.core.sync.ConflictResolution.USE_REMOTE) {
                debtCreditDao.upsertDebtCredit(remote.copy(syncStatus = SyncState.SYNCED).toEntity())
            }
        }
    }

    override fun observeTotalPendingCount(userId: String): Flow<Int> {
        return combine(
            listOf(
                userDao.observePendingCount(userId),
                expenseDao.observePendingCount(userId),
                categoryDao.observePendingCount(userId),
                weeklyTargetDao.observePendingCount(userId),
                monthlyTargetDao.observePendingCount(userId),
                salaryHandRecordDao.observePendingCount(userId),
                debtCreditDao.observePendingCount(userId)
            )
        ) { counts: Array<Int> ->
            counts.sum()
        }
    }
}
