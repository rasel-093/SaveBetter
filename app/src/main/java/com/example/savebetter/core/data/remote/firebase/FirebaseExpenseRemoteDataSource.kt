package com.example.savebetter.core.data.remote.firebase

import com.example.savebetter.core.data.mapper.toDomain
import com.example.savebetter.core.data.mapper.toFirestoreDto
import com.example.savebetter.core.data.remote.ExpenseRemoteDataSource
import com.example.savebetter.core.data.remote.firebase.dto.ExpenseFirestoreDto
import com.example.savebetter.core.domain.model.Expense
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Firestore implementation for expense sync.
 *
 * User expenses are strictly isolated to `/users/{userId}/expenses/{expenseId}`.
 */
@Singleton
class FirebaseExpenseRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : ExpenseRemoteDataSource {

    override suspend fun fetchExpenses(userId: String): Result<List<Expense>> = runCatching {
        val querySnapshot = firestore.collection("users")
            .document(userId)
            .collection("expenses")
            .get()
            .await()

        querySnapshot.documents.mapNotNull { doc ->
            doc.toObject(ExpenseFirestoreDto::class.java)?.toDomain()
        }
    }


    override suspend fun uploadExpense(expense: Expense): Result<Unit> = runCatching {
        val dto = expense.toFirestoreDto()
        firestore.collection("users")
            .document(expense.userId)
            .collection("expenses")
            .document(expense.id)
            .set(dto, SetOptions.merge())
            .await()
    }

    override suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> = runCatching {
        firestore.collection("users")
            .document(userId)
            .collection("expenses")
            .document(expenseId)
            .update(
                mapOf(
                    "isDeleted" to true,
                    "deletedAtEpochMilli" to System.currentTimeMillis()
                )
            )
            .await()
    }
}
