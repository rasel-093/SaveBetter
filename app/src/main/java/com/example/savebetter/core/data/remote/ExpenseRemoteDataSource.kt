package com.example.savebetter.core.data.remote

import com.example.savebetter.core.domain.model.Expense

/**
 * Backend-agnostic remote data source interface for expense transactions.
 *
 * Current implementation: [com.example.savebetter.core.data.remote.firebase.FirebaseExpenseRemoteDataSource]
 * Future implementation: Django REST API / PostgreSQL data source.
 */
interface ExpenseRemoteDataSource {
    suspend fun fetchExpenses(userId: String): Result<List<Expense>>
    suspend fun uploadExpense(expense: Expense): Result<Unit>
    suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit>
}
