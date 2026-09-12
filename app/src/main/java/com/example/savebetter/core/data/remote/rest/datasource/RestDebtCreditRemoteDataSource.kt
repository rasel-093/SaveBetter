package com.example.savebetter.core.data.remote.rest.datasource

import com.example.savebetter.core.data.remote.DebtCreditRemoteDataSource
import com.example.savebetter.core.data.remote.rest.api.SaveBetterRestApi
import com.example.savebetter.core.data.remote.rest.mapper.toDomain
import com.example.savebetter.core.data.remote.rest.mapper.toRestDto
import com.example.savebetter.core.domain.model.DebtCredit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RESTful backend implementation of [DebtCreditRemoteDataSource].
 */
@Singleton
class RestDebtCreditRemoteDataSource @Inject constructor(
    private val restApi: SaveBetterRestApi
) : DebtCreditRemoteDataSource {

    override suspend fun fetchDebtsAndCredits(userId: String): Result<List<DebtCredit>> {
        return restApi.fetchDebtsAndCredits(userId).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun uploadDebtCredit(item: DebtCredit): Result<Unit> {
        return restApi.uploadDebtCredit(item.userId, item.toRestDto())
    }

    override suspend fun deleteDebtCredit(userId: String, id: String): Result<Unit> {
        return restApi.deleteDebtCredit(userId, id)
    }
}
