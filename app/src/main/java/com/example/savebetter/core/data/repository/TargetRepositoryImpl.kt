package com.example.savebetter.core.data.repository

import com.example.savebetter.core.data.local.dao.MonthlyTargetDao
import com.example.savebetter.core.data.local.dao.SalaryHandRecordDao
import com.example.savebetter.core.data.local.dao.WeeklyTargetDao
import com.example.savebetter.core.data.mapper.toDomain
import com.example.savebetter.core.data.mapper.toEntity
import com.example.savebetter.core.data.remote.TargetRemoteDataSource
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.WeeklyTarget
import com.example.savebetter.core.domain.repository.TargetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [TargetRepository].
 */
@Singleton
class TargetRepositoryImpl @Inject constructor(
    private val weeklyTargetDao: WeeklyTargetDao,
    private val monthlyTargetDao: MonthlyTargetDao,
    private val salaryHandRecordDao: SalaryHandRecordDao,
    private val remoteDataSource: TargetRemoteDataSource
) : TargetRepository {

    override fun observeWeeklyTargets(userId: String): Flow<List<WeeklyTarget>> =
        weeklyTargetDao.observeWeeklyTargets(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun getWeeklyTargetForDate(userId: String, date: String): WeeklyTarget? =
        weeklyTargetDao.getWeeklyTargetForDate(userId, date)?.toDomain()

    override suspend fun saveWeeklyTarget(target: WeeklyTarget) {
        val pending = target.copy(syncStatus = SyncState.PENDING, updatedAt = Instant.now())
        weeklyTargetDao.upsertWeeklyTarget(pending.toEntity())

        remoteDataSource.uploadWeeklyTarget(pending).onSuccess {
            weeklyTargetDao.upsertWeeklyTarget(pending.copy(syncStatus = SyncState.SYNCED).toEntity())
        }
    }

    override fun observeMonthlyTargets(userId: String): Flow<List<MonthlyTarget>> =
        monthlyTargetDao.observeMonthlyTargets(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun getMonthlyTarget(userId: String, year: Int, month: Int): MonthlyTarget? =
        monthlyTargetDao.getMonthlyTarget(userId, year, month)?.toDomain()

    override suspend fun saveMonthlyTarget(target: MonthlyTarget) {
        val pending = target.copy(syncStatus = SyncState.PENDING, updatedAt = Instant.now())
        monthlyTargetDao.upsertMonthlyTarget(pending.toEntity())

        remoteDataSource.uploadMonthlyTarget(pending).onSuccess {
            monthlyTargetDao.upsertMonthlyTarget(pending.copy(syncStatus = SyncState.SYNCED).toEntity())
        }
    }

    override fun observeSalaryHandRecords(userId: String): Flow<List<SalaryHandRecord>> =
        salaryHandRecordDao.observeSalaryHandRecords(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun getSalaryHandRecord(userId: String, year: Int, month: Int): SalaryHandRecord? =
        salaryHandRecordDao.getSalaryHandRecord(userId, year, month)?.toDomain()

    override suspend fun saveSalaryHandRecord(record: SalaryHandRecord) {
        val pending = record.copy(syncStatus = SyncState.PENDING, updatedAt = Instant.now())
        salaryHandRecordDao.upsertSalaryHandRecord(pending.toEntity())

        remoteDataSource.uploadSalaryHandRecord(pending).onSuccess {
            salaryHandRecordDao.upsertSalaryHandRecord(pending.copy(syncStatus = SyncState.SYNCED).toEntity())
        }
    }

    override suspend fun syncPendingTargets(userId: String): Result<Unit> = runCatching {
        weeklyTargetDao.getPendingWeeklyTargets(userId).forEach { item ->
            val domain = item.toDomain()
            remoteDataSource.uploadWeeklyTarget(domain).onSuccess {
                weeklyTargetDao.upsertWeeklyTarget(domain.copy(syncStatus = SyncState.SYNCED).toEntity())
            }
        }
        monthlyTargetDao.getPendingMonthlyTargets(userId).forEach { item ->
            val domain = item.toDomain()
            remoteDataSource.uploadMonthlyTarget(domain).onSuccess {
                monthlyTargetDao.upsertMonthlyTarget(domain.copy(syncStatus = SyncState.SYNCED).toEntity())
            }
        }
        salaryHandRecordDao.getPendingSalaryHandRecords(userId).forEach { item ->
            val domain = item.toDomain()
            remoteDataSource.uploadSalaryHandRecord(domain).onSuccess {
                salaryHandRecordDao.upsertSalaryHandRecord(domain.copy(syncStatus = SyncState.SYNCED).toEntity())
            }
        }
    }
}
