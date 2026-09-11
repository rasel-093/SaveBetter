package com.example.savebetter.core.data.repository

import com.example.savebetter.core.data.local.dao.DebtCreditDao
import com.example.savebetter.core.data.mapper.toDomain
import com.example.savebetter.core.data.mapper.toEntity
import com.example.savebetter.core.data.remote.DebtCreditRemoteDataSource
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.DebtCreditRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [DebtCreditRepository].
 */
@Singleton
class DebtCreditRepositoryImpl @Inject constructor(
    private val debtCreditDao: DebtCreditDao,
    private val remoteDataSource: DebtCreditRemoteDataSource
) : DebtCreditRepository {

    override fun observeDebtsAndCredits(userId: String): Flow<List<DebtCredit>> =
        debtCreditDao.observeDebtsAndCredits(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun getDebtCreditById(id: String): DebtCredit? =
        debtCreditDao.getDebtCreditById(id)?.toDomain()

    override suspend fun addDebtCredit(item: DebtCredit) {
        val pending = item.copy(syncStatus = SyncState.PENDING, updatedAt = Instant.now())
        debtCreditDao.upsertDebtCredit(pending.toEntity())

        remoteDataSource.uploadDebtCredit(pending).onSuccess {
            debtCreditDao.upsertDebtCredit(pending.copy(syncStatus = SyncState.SYNCED).toEntity())
        }
    }

    override suspend fun updateDebtCredit(item: DebtCredit) {
        val pending = item.copy(syncStatus = SyncState.PENDING, updatedAt = Instant.now())
        debtCreditDao.upsertDebtCredit(pending.toEntity())

        remoteDataSource.uploadDebtCredit(pending).onSuccess {
            debtCreditDao.upsertDebtCredit(pending.copy(syncStatus = SyncState.SYNCED).toEntity())
        }
    }

    override suspend fun markSettled(id: String, isSettled: Boolean) {
        val now = Instant.now()
        debtCreditDao.updateSettled(id, isSettled, updatedAt = now, syncStatus = SyncState.PENDING)
        debtCreditDao.getDebtCreditById(id)?.let { entity ->
            remoteDataSource.uploadDebtCredit(entity.toDomain()).onSuccess {
                debtCreditDao.updateSettled(id, isSettled, updatedAt = now, syncStatus = SyncState.SYNCED)
            }
        }
    }

    override suspend fun deleteDebtCredit(id: String) {
        val now = Instant.now()
        val item = debtCreditDao.getDebtCreditById(id) ?: return
        debtCreditDao.softDeleteDebtCredit(id, deletedAt = now, updatedAt = now, syncStatus = SyncState.PENDING)

        remoteDataSource.deleteDebtCredit(item.userId, id).onSuccess {
            debtCreditDao.softDeleteDebtCredit(id, deletedAt = now, updatedAt = now, syncStatus = SyncState.SYNCED)
        }
    }

    override suspend fun syncPendingDebtsAndCredits(userId: String): Result<Unit> = runCatching {
        val pending = debtCreditDao.getPendingDebtsAndCredits(userId)
        for (item in pending) {
            val domain = item.toDomain()
            if (domain.isDeleted) {
                remoteDataSource.deleteDebtCredit(userId, domain.id).onSuccess {
                    debtCreditDao.softDeleteDebtCredit(domain.id, domain.deletedAt ?: Instant.now(), Instant.now(), SyncState.SYNCED)
                }
            } else {
                remoteDataSource.uploadDebtCredit(domain).onSuccess {
                    debtCreditDao.upsertDebtCredit(domain.copy(syncStatus = SyncState.SYNCED).toEntity())
                }
            }
        }
    }
}
