package com.example.savebetter.core.data.remote

import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.WeeklyTarget

/**
 * Backend-agnostic remote data source interface for budget targets and salary records.
 */
interface TargetRemoteDataSource {
    suspend fun fetchWeeklyTargets(userId: String): Result<List<WeeklyTarget>>
    suspend fun uploadWeeklyTarget(target: WeeklyTarget): Result<Unit>

    suspend fun fetchMonthlyTargets(userId: String): Result<List<MonthlyTarget>>
    suspend fun uploadMonthlyTarget(target: MonthlyTarget): Result<Unit>

    suspend fun fetchSalaryHandRecords(userId: String): Result<List<SalaryHandRecord>>
    suspend fun uploadSalaryHandRecord(record: SalaryHandRecord): Result<Unit>
}
