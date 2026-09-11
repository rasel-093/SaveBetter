package com.example.savebetter.core.data.repository

import com.example.savebetter.core.data.local.dao.ExpenseDao
import com.example.savebetter.core.data.mapper.toDomain
import com.example.savebetter.core.data.mapper.toEntity
import com.example.savebetter.core.data.remote.ExpenseRemoteDataSource
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [ExpenseRepository].
 *
 * Saves to Room immediately so UI updates with zero latency.
 */
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val remoteDataSource: ExpenseRemoteDataSource
) : ExpenseRepository {

    override fun observeExpenses(userId: String): Flow<List<Expense>> =
        expenseDao.observeExpenses(userId).map { list -> list.map { it.toDomain() } }

    override fun observeExpensesByDateRange(
        userId: String,
        startDate: Instant,
        endDate: Instant
    ): Flow<List<Expense>> =
        expenseDao.observeExpensesByDateRange(userId, startDate, endDate).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getExpenseById(id: String): Expense? =
        expenseDao.getExpenseById(id)?.toDomain()

    override suspend fun addExpense(expense: Expense) {
        val pending = expense.copy(syncStatus = SyncState.PENDING, updatedAt = Instant.now())
        expenseDao.insertExpense(pending.toEntity())

        remoteDataSource.uploadExpense(pending).onSuccess {
            expenseDao.updateExpense(pending.copy(syncStatus = SyncState.SYNCED).toEntity())
        }.onFailure {
            // Keep PENDING for background SyncWorker
        }
    }

    override suspend fun updateExpense(expense: Expense) {
        val pending = expense.copy(syncStatus = SyncState.PENDING, updatedAt = Instant.now())
        expenseDao.updateExpense(pending.toEntity())

        remoteDataSource.uploadExpense(pending).onSuccess {
            expenseDao.updateExpense(pending.copy(syncStatus = SyncState.SYNCED).toEntity())
        }
    }

    override suspend fun deleteExpense(id: String) {
        val now = Instant.now()
        val expense = expenseDao.getExpenseById(id) ?: return
        expenseDao.softDeleteExpense(id, deletedAt = now, updatedAt = now, syncStatus = SyncState.PENDING)

        remoteDataSource.deleteExpense(expense.userId, id).onSuccess {
            expenseDao.softDeleteExpense(id, deletedAt = now, updatedAt = now, syncStatus = SyncState.SYNCED)
        }
    }

    override suspend fun syncPendingExpenses(userId: String): Result<Unit> = runCatching {
        val pending = expenseDao.getPendingExpenses(userId)
        for (item in pending) {
            val domain = item.toDomain()
            if (domain.isDeleted) {
                remoteDataSource.deleteExpense(userId, domain.id).onSuccess {
                    expenseDao.softDeleteExpense(domain.id, domain.deletedAt ?: Instant.now(), Instant.now(), SyncState.SYNCED)
                }
            } else {
                remoteDataSource.uploadExpense(domain).onSuccess {
                    expenseDao.updateExpense(domain.copy(syncStatus = SyncState.SYNCED).toEntity())
                }
            }
        }
    }
}
