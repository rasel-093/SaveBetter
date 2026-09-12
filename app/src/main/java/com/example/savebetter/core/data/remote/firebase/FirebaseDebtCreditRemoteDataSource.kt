package com.example.savebetter.core.data.remote.firebase

import com.example.savebetter.core.data.mapper.toDomain
import com.example.savebetter.core.data.mapper.toFirestoreDto
import com.example.savebetter.core.data.remote.DebtCreditRemoteDataSource
import com.example.savebetter.core.data.remote.firebase.dto.DebtCreditFirestoreDto
import com.example.savebetter.core.domain.model.DebtCredit
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Firestore implementation for debt and credit sync.
 */
@Singleton
class FirebaseDebtCreditRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : DebtCreditRemoteDataSource {

    override suspend fun fetchDebtsAndCredits(userId: String): Result<List<DebtCredit>> = runCatching {
        val querySnapshot = firestore.collection("users")
            .document(userId)
            .collection("debts")
            .get()
            .await()

        querySnapshot.documents.mapNotNull { doc ->
            doc.toObject(DebtCreditFirestoreDto::class.java)?.toDomain()
        }
    }

    override suspend fun uploadDebtCredit(item: DebtCredit): Result<Unit> = runCatching {
        val dto = item.toFirestoreDto()
        firestore.collection("users")
            .document(item.userId)
            .collection("debts")
            .document(item.id)
            .set(dto, SetOptions.merge())
            .await()
    }

    override suspend fun deleteDebtCredit(userId: String, id: String): Result<Unit> = runCatching {
        firestore.collection("users")
            .document(userId)
            .collection("debts")
            .document(id)
            .update(
                mapOf(
                    "isDeleted" to true,
                    "deletedAtEpochMilli" to System.currentTimeMillis()
                )
            )
            .await()
    }
}
