package com.example.savebetter.core.data.remote.rest.datasource

import com.example.savebetter.core.data.remote.TargetRemoteDataSource
import com.example.savebetter.core.data.remote.rest.api.SaveBetterRestApi
import com.example.savebetter.core.data.remote.rest.mapper.toDomain
import com.example.savebetter.core.data.remote.rest.mapper.toRestDto
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.WeeklyTarget
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RESTful backend implementation of [TargetRemoteDataSource].
 */
@Singleton
class RestTargetRemoteDataSource @Inject constructor(
    private val restApi: SaveBetterRestApi
) : TargetRemoteDataSource {

    override suspend fun fetchWeeklyTargets(userId: String): Result<List<WeeklyTarget>> {
        return restApi.fetchWeeklyTargets(userId).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun uploadWeeklyTarget(target: WeeklyTarget): Result<Unit> {
        return restApi.uploadWeeklyTarget(target.userId, target.toRestDto())
    }

    override suspend fun fetchMonthlyTargets(userId: String): Result<List<MonthlyTarget>> {
        return restApi.fetchMonthlyTargets(userId).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun uploadMonthlyTarget(target: MonthlyTarget): Result<Unit> {
        return restApi.uploadMonthlyTarget(target.userId, target.toRestDto())
    }

    override suspend fun fetchSalaryHandRecords(userId: String): Result<List<SalaryHandRecord>> {
        return restApi.fetchSalaryHandRecords(userId).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun uploadSalaryHandRecord(record: SalaryHandRecord): Result<Unit> {
        return restApi.uploadSalaryHandRecord(record.userId, record.toRestDto())
    }
}
