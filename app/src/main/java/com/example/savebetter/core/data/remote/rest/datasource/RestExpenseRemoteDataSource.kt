package com.example.savebetter.core.data.remote.rest.datasource

import com.example.savebetter.core.data.remote.ExpenseRemoteDataSource
import com.example.savebetter.core.data.remote.rest.api.SaveBetterRestApi
import com.example.savebetter.core.data.remote.rest.mapper.toDomain
import com.example.savebetter.core.data.remote.rest.mapper.toRestDto
import com.example.savebetter.core.domain.model.Expense
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RESTful backend implementation of [ExpenseRemoteDataSource].
 *
 * Plugs directly into SaveBetter without modifying Room entities, DAOs, or UI.
 */
@Singleton
class RestExpenseRemoteDataSource @Inject constructor(
    private val restApi: SaveBetterRestApi
) : ExpenseRemoteDataSource {

    override suspend fun fetchExpenses(userId: String): Result<List<Expense>> {
        return restApi.fetchExpenses(userId).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun uploadExpense(expense: Expense): Result<Unit> {
        return restApi.uploadExpense(expense.userId, expense.toRestDto())
    }

    override suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> {
        return restApi.deleteExpense(userId, expenseId)
    }
}
