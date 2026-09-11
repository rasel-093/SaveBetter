package com.example.savebetter.core.domain.repository

import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.WeeklyTarget
import kotlinx.coroutines.flow.Flow

/**
 * Backend-agnostic repository interface for weekly/monthly targets and salary hand records.
 */
interface TargetRepository {
    fun observeWeeklyTargets(userId: String): Flow<List<WeeklyTarget>>
    suspend fun getWeeklyTargetForDate(userId: String, date: String): WeeklyTarget?
    suspend fun saveWeeklyTarget(target: WeeklyTarget)

    fun observeMonthlyTargets(userId: String): Flow<List<MonthlyTarget>>
    suspend fun getMonthlyTarget(userId: String, year: Int, month: Int): MonthlyTarget?
    suspend fun saveMonthlyTarget(target: MonthlyTarget)

    fun observeSalaryHandRecords(userId: String): Flow<List<SalaryHandRecord>>
    suspend fun getSalaryHandRecord(userId: String, year: Int, month: Int): SalaryHandRecord?
    suspend fun saveSalaryHandRecord(record: SalaryHandRecord)

    suspend fun syncPendingTargets(userId: String): Result<Unit>
}
