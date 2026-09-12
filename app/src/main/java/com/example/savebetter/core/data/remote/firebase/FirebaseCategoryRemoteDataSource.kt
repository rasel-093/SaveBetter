package com.example.savebetter.core.data.remote.firebase

import com.example.savebetter.core.data.mapper.toDomain
import com.example.savebetter.core.data.mapper.toFirestoreDto
import com.example.savebetter.core.data.remote.CategoryRemoteDataSource
import com.example.savebetter.core.data.remote.firebase.dto.CategoryFirestoreDto
import com.example.savebetter.core.domain.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Firestore implementation for category sync.
 *
 * Categories are isolated under `/users/{userId}/categories/{categoryId}`.
 */
@Singleton
class FirebaseCategoryRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoryRemoteDataSource {

    override suspend fun fetchCategories(userId: String): Result<List<Category>> = runCatching {
        val querySnapshot = firestore.collection("users")
            .document(userId)
            .collection("categories")
            .get()
            .await()

        querySnapshot.documents.mapNotNull { doc ->
            doc.toObject(CategoryFirestoreDto::class.java)?.toDomain()
        }
    }


    override suspend fun uploadCategory(category: Category): Result<Unit> = runCatching {
        val dto = category.toFirestoreDto()
        firestore.collection("users")
            .document(category.userId)
            .collection("categories")
            .document(category.id)
            .set(dto, SetOptions.merge())
            .await()
    }

    override suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit> = runCatching {
        firestore.collection("users")
            .document(userId)
            .collection("categories")
            .document(categoryId)
            .update(
                mapOf("isDeleted" to true)
            )
            .await()
    }
}
